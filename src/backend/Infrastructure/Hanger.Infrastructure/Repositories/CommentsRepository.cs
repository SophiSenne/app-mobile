using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class CommentsRepository(NpgsqlDataSource dataSource) : ICommentsRepository
{
    public async Task<IReadOnlyList<CommentDto>> GetByPostIdAsync(
        Guid postId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT c.id, c.post_id, c.user_id, u.username, u.avatar_url, c.content, c.created_at
            FROM comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.post_id = @PostId
            ORDER BY c.created_at ASC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<CommentDto>(sql, new { PostId = postId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<CommentDto?> GetByIdAsync(Guid commentId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT c.id, c.post_id, c.user_id, u.username, u.avatar_url, c.content, c.created_at
            FROM comments c
            JOIN users u ON u.id = c.user_id
            WHERE c.id = @CommentId
            """;

        return await connection.QuerySingleOrDefaultAsync<CommentDto>(sql, new { CommentId = commentId });
    }

    public async Task<CommentDto> CreateAsync(
        Guid postId, Guid userId, CreateCommentRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string insertSql = """
            INSERT INTO comments (post_id, user_id, content)
            VALUES (@PostId, @UserId, @Content)
            RETURNING id
            """;

        var commentId = await connection.ExecuteScalarAsync<Guid>(insertSql, new
        {
            PostId = postId,
            UserId = userId,
            request.Content
        });

        return (await GetByIdAsync(commentId, cancellationToken))!;
    }

    public async Task<CommentDto?> UpdateAsync(
        Guid commentId, Guid userId, UpdateCommentRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            UPDATE comments
            SET content = @Content
            WHERE id = @CommentId AND user_id = @UserId
            """;

        var affected = await connection.ExecuteAsync(sql, new { CommentId = commentId, UserId = userId, request.Content });

        if (affected == 0)
            return null;

        return await GetByIdAsync(commentId, cancellationToken);
    }

    public async Task<bool> DeleteAsync(Guid commentId, Guid userId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM comments WHERE id = @CommentId AND user_id = @UserId";
        var affected = await connection.ExecuteAsync(sql, new { CommentId = commentId, UserId = userId });
        return affected > 0;
    }

    public async Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(*)::INT FROM comments WHERE post_id = @PostId";
        return await connection.ExecuteScalarAsync<int>(sql, new { PostId = postId });
    }
}
