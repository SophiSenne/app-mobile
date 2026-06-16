using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class PostsRepository(NpgsqlDataSource dataSource) : IPostsRepository
{
    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public async Task<IReadOnlyList<PostDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken)
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
            FROM posts p
            JOIN users u ON u.id = p.user_id
            LEFT JOIN post_tags pt ON pt.post_id = p.id
            LEFT JOIN categories c ON c.id = pt.category_id
            LEFT JOIN types t ON t.id = pt.type_id
            ORDER BY p.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<PostDto>(sql, new { Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<IReadOnlyList<PostDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken)
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
            FROM posts p
            JOIN users u ON u.id = p.user_id
            LEFT JOIN post_tags pt ON pt.post_id = p.id
            LEFT JOIN categories c ON c.id = pt.category_id
            LEFT JOIN types t ON t.id = pt.type_id
            WHERE p.user_id = @UserId
            ORDER BY p.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<PostDto>(sql, new { UserId = userId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<PostDto?> GetByIdAsync(Guid postId, CancellationToken cancellationToken)
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
            FROM posts p
            JOIN users u ON u.id = p.user_id
            LEFT JOIN post_tags pt ON pt.post_id = p.id
            LEFT JOIN categories c ON c.id = pt.category_id
            LEFT JOIN types t ON t.id = pt.type_id
            WHERE p.id = @PostId
            """;

        return await connection.QuerySingleOrDefaultAsync<PostDto>(sql, new { PostId = postId });
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public async Task<PostDto> CreateAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);
        await using var transaction = await connection.BeginTransactionAsync(cancellationToken);

        const string insertPost = """
            INSERT INTO posts (user_id, image_url, title, caption, weather_condition, temperature, city)
            VALUES (@UserId, @ImageUrl, @Title, @Caption, @WeatherCondition, @Temperature, @City)
            RETURNING id
            """;

        var postId = await connection.ExecuteScalarAsync<Guid>(insertPost, new
        {
            UserId = userId,
            request.ImageUrl,
            request.Title,
            request.Caption,
            request.WeatherCondition,
            request.Temperature,
            request.City
        }, transaction);

        if (request.CategoryId.HasValue)
        {
            const string insertTag = """
                INSERT INTO post_tags (post_id, category_id, type_id)
                VALUES (@PostId, @CategoryId, @TypeId)
                """;

            await connection.ExecuteAsync(insertTag, new
            {
                PostId = postId,
                CategoryId = request.CategoryId.Value,
                TypeId = request.TypeId
            }, transaction);
        }

        await transaction.CommitAsync(cancellationToken);

        return (await GetByIdAsync(postId, cancellationToken))!;
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public async Task<PostDto?> UpdateAsync(Guid postId, Guid userId, UpdatePostRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);
        await using var transaction = await connection.BeginTransactionAsync(cancellationToken);

        const string updatePost = """
            UPDATE posts
            SET image_url         = COALESCE(@ImageUrl, image_url),
                title             = COALESCE(@Title, title),
                caption           = @Caption,
                weather_condition = @WeatherCondition,
                temperature       = @Temperature,
                city              = @City
            WHERE id = @PostId AND user_id = @UserId
            """;

        var affected = await connection.ExecuteAsync(updatePost, new
        {
            PostId = postId,
            UserId = userId,
            request.ImageUrl,
            request.Title,
            request.Caption,
            request.WeatherCondition,
            request.Temperature,
            request.City
        }, transaction);

        if (affected == 0)
        {
            await transaction.RollbackAsync(cancellationToken);
            return null;
        }

        if (request.CategoryId.HasValue)
        {
            const string upsertTag = """
                INSERT INTO post_tags (post_id, category_id, type_id)
                VALUES (@PostId, @CategoryId, @TypeId)
                ON CONFLICT (post_id, category_id) DO UPDATE
                    SET type_id = EXCLUDED.type_id
                """;

            await connection.ExecuteAsync(upsertTag, new
            {
                PostId = postId,
                CategoryId = request.CategoryId.Value,
                TypeId = request.TypeId
            }, transaction);
        }

        await transaction.CommitAsync(cancellationToken);

        return await GetByIdAsync(postId, cancellationToken);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public async Task<bool> DeleteAsync(Guid postId, Guid userId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM posts WHERE id = @PostId AND user_id = @UserId";
        var affected = await connection.ExecuteAsync(sql, new { PostId = postId, UserId = userId });
        return affected > 0;
    }

    // -------------------------------------------------------------------------
    // EXISTS
    // -------------------------------------------------------------------------

    public async Task<bool> ExistsAsync(Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM posts WHERE id = @PostId";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { PostId = postId });
        return count > 0;
    }
}