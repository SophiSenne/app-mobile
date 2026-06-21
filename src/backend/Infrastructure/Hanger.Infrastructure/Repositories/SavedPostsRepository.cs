using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class SavedPostsRepository(NpgsqlDataSource dataSource) : ISavedPostsRepository
{
    public async Task<IReadOnlyList<PostDto>> GetByUserIdAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT
                p.id, p.user_id, u.username,
                p.image_url, p.title, p.caption,
                p.weather_condition, p.temperature, p.city,
                p.share_count, p.created_at,
                pt.category_id, c.name AS category_name,
                pt.type_id, t.name AS type_name
            FROM saved_posts sp
            JOIN posts p ON p.id = sp.post_id
            JOIN users u ON u.id = p.user_id
            LEFT JOIN post_tags pt ON pt.post_id = p.id
            LEFT JOIN categories c ON c.id = pt.category_id
            LEFT JOIN types t ON t.id = pt.type_id
            WHERE sp.user_id = @UserId
            ORDER BY sp.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<PostDto>(sql, new { UserId = userId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<bool> ExistsAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM saved_posts WHERE user_id = @UserId AND post_id = @PostId";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { UserId = userId, PostId = postId });
        return count > 0;
    }

    public async Task<SavedPostDto> SaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        if (await ExistsAsync(userId, postId, cancellationToken))
            throw new InvalidOperationException("Você já salvou este post.");

        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string insertSql = """
            INSERT INTO saved_posts (user_id, post_id)
            VALUES (@UserId, @PostId)
            """;

        const string selectSql = """
            SELECT sp.user_id, u.username, sp.post_id, sp.created_at
            FROM saved_posts sp
            JOIN users u ON u.id = sp.user_id
            WHERE sp.user_id = @UserId AND sp.post_id = @PostId
            """;

        await connection.ExecuteAsync(insertSql, new { UserId = userId, PostId = postId });
        return await connection.QuerySingleAsync<SavedPostDto>(selectSql, new { UserId = userId, PostId = postId });
    }

    public async Task<bool> UnsaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM saved_posts WHERE user_id = @UserId AND post_id = @PostId";
        var affected = await connection.ExecuteAsync(sql, new { UserId = userId, PostId = postId });
        return affected > 0;
    }

    public async Task<int> CountByUserIdAsync(Guid userId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(*)::INT FROM saved_posts WHERE user_id = @UserId";
        return await connection.ExecuteScalarAsync<int>(sql, new { UserId = userId });
    }
}
