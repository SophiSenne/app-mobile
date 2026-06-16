using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class NotificationsService(INotificationsRepository repository) : INotificationsService
{
    public Task<IReadOnlyList<NotificationDto>> GetAllAsync(
        Guid recipientId, int limit, int offset, CancellationToken cancellationToken) =>
        repository.GetByRecipientAsync(recipientId, limit, offset, cancellationToken);

    public Task<IReadOnlyList<NotificationDto>> GetUnreadAsync(
        Guid recipientId, CancellationToken cancellationToken) =>
        repository.GetUnreadByRecipientAsync(recipientId, cancellationToken);

    public Task<int> CountUnreadAsync(Guid recipientId, CancellationToken cancellationToken) =>
        repository.CountUnreadAsync(recipientId, cancellationToken);

    public Task<bool> MarkAsReadAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken) =>
        repository.MarkAsReadAsync(notificationId, recipientId, cancellationToken);

    public Task<int> MarkAllAsReadAsync(Guid recipientId, CancellationToken cancellationToken) =>
        repository.MarkAllAsReadAsync(recipientId, cancellationToken);

    public Task<bool> DeleteAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken) =>
        repository.DeleteAsync(notificationId, recipientId, cancellationToken);
}
