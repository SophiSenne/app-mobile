using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface INotificationsService
{
    Task<IReadOnlyList<NotificationDto>> GetAllAsync(Guid recipientId, int limit, int offset, CancellationToken cancellationToken);

    Task<IReadOnlyList<NotificationDto>> GetUnreadAsync(Guid recipientId, CancellationToken cancellationToken);

    Task<int> CountUnreadAsync(Guid recipientId, CancellationToken cancellationToken);

    Task<bool> MarkAsReadAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken);

    Task<int> MarkAllAsReadAsync(Guid recipientId, CancellationToken cancellationToken);

    Task<bool> DeleteAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken);
}
