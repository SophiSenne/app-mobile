using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface INotificationsRepository
{
    Task<IReadOnlyList<NotificationDto>> GetByRecipientAsync(Guid recipientId, int limit, int offset, CancellationToken cancellationToken);

    Task<IReadOnlyList<NotificationDto>> GetUnreadByRecipientAsync(Guid recipientId, CancellationToken cancellationToken);

    Task<int> CountUnreadAsync(Guid recipientId, CancellationToken cancellationToken);

    /// <summary>Marca uma notificação como lida. Retorna false se não encontrada ou não pertence ao usuário.</summary>
    Task<bool> MarkAsReadAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken);

    /// <summary>Marca todas as notificações do usuário como lidas. Retorna a quantidade afetada.</summary>
    Task<int> MarkAllAsReadAsync(Guid recipientId, CancellationToken cancellationToken);

    Task<bool> DeleteAsync(Guid notificationId, Guid recipientId, CancellationToken cancellationToken);
}
