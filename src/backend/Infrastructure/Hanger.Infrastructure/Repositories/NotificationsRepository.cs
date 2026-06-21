using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class NotificationsRepository(NpgsqlDataSource dataSource) : INotificationsRepository
{
    private const string SelectBase = """
        SELECT
            n.id, n.recipient_id, n.sender_id,
            u.username AS sender_username,
            u.avatar_url AS sender_avatar_url,
            n.type::TEXT AS type,
            n.post_id, n.read, n.created_at
        FROM notifications n
        JOIN users u ON u.id = n.sender_id
        """;

    public async Task<IReadOnlyList<NotificationDto>> GetByRecipientAsync(
        Guid recipientId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        var sql = $"""
            {SelectBase}
            WHERE n.recipient_id = @RecipientId
            ORDER BY n.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<NotificationDto>(sql, new { RecipientId = recipientId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<IReadOnlyList<NotificationDto>> GetUnreadByRecipientAsync(
        Guid recipientId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        var sql = $"""
            {SelectBase}
            WHERE n.recipient_id = @RecipientId AND n.read = FALSE
            ORDER BY n.created_at DESC
            """;

        var result = await connection.QueryAsync<NotificationDto>(sql, new { RecipientId = recipientId });
        return result.ToList();
    }

    public async Task<int> CountUnreadAsync(Guid recipientId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT COUNT(*)::INT FROM notifications
            WHERE recipient_id = @RecipientId AND read = FALSE
            """;

        return await connection.ExecuteScalarAsync<int>(sql, new { RecipientId = recipientId });
    }

    public async Task<bool> MarkAsReadAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            UPDATE notifications
            SET read = TRUE
            WHERE id = @NotificationId AND recipient_id = @RecipientId
            """;

        var affected = await connection.ExecuteAsync(sql, new { NotificationId = notificationId, RecipientId = recipientId });
        return affected > 0;
    }

    public async Task<int> MarkAllAsReadAsync(Guid recipientId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            UPDATE notifications
            SET read = TRUE
            WHERE recipient_id = @RecipientId AND read = FALSE
            """;

        return await connection.ExecuteAsync(sql, new { RecipientId = recipientId });
    }

    public async Task<bool> DeleteAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            DELETE FROM notifications
            WHERE id = @NotificationId AND recipient_id = @RecipientId
            """;

        var affected = await connection.ExecuteAsync(sql, new { NotificationId = notificationId, RecipientId = recipientId });
        return affected > 0;
    }

    public async Task<NotificationDto> CreateAsync(
        Guid recipientId, Guid senderId, string type, Guid? postId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string insertSql = """
            INSERT INTO notifications (recipient_id, sender_id, type, post_id)
            VALUES (@RecipientId, @SenderId, @Type::notification_type, @PostId)
            RETURNING id, created_at
            """;

        var row = await connection.QuerySingleAsync<(Guid Id, DateTime CreatedAt)>(
            insertSql,
            new { RecipientId = recipientId, SenderId = senderId, Type = type, PostId = postId });

        var selectSql = $"""
            {SelectBase}
            WHERE n.id = @Id
            """;

        return await connection.QuerySingleAsync<NotificationDto>(selectSql, new { row.Id });
    }
}
