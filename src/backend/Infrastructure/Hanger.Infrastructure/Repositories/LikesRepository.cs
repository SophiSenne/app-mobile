using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class LikesRepository(NpgsqlDataSource dataSource) : ILikesRepository
{
    public async Task<IReadOnlyList<LikeDto>> GetByPostIdAsync(
        Guid postId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT l.user_id, u.username, l.post_id, l.created_at
            FROM likes l
            JOIN users u ON u.id = l.user_id
            WHERE l.post_id = @PostId
            ORDER BY l.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<LikeDto>(sql, new { PostId = postId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<IReadOnlyList<LikeDto>> GetByUserIdAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT l.user_id, u.username, l.post_id, l.created_at
            FROM likes l
            JOIN users u ON u.id = l.user_id
            WHERE l.user_id = @UserId
            ORDER BY l.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<LikeDto>(sql, new { UserId = userId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<bool> ExistsAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM likes WHERE user_id = @UserId AND post_id = @PostId";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { UserId = userId, PostId = postId });
        return count > 0;
    }

    public async Task<LikeDto> LikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        if (await ExistsAsync(userId, postId, cancellationToken))
            throw new InvalidOperationException("Você já curtiu este post.");

        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string insertSql = """
            INSERT INTO likes (user_id, post_id)
            VALUES (@UserId, @PostId)
            """;

        const string selectSql = """
            SELECT l.user_id, u.username, l.post_id, l.created_at
            FROM likes l
            JOIN users u ON u.id = l.user_id
            WHERE l.user_id = @UserId AND l.post_id = @PostId
            """;

        await connection.ExecuteAsync(insertSql, new { UserId = userId, PostId = postId });
        return await connection.QuerySingleAsync<LikeDto>(selectSql, new { UserId = userId, PostId = postId });
    }

    public async Task<bool> UnlikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM likes WHERE user_id = @UserId AND post_id = @PostId";
        var affected = await connection.ExecuteAsync(sql, new { UserId = userId, PostId = postId });
        return affected > 0;
    }

    public async Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(*)::INT FROM likes WHERE post_id = @PostId";
        return await connection.ExecuteScalarAsync<int>(sql, new { PostId = postId });
    }
}
