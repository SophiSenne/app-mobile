using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("notifications")]
public class NotificationsController(INotificationsService service) : ApiControllerBase
{
    /// <summary>Listar todas as notificações do usuário autenticado.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<NotificationDto>>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> GetAll(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var notifications = await service.GetAllAsync(userId, limit, offset, cancellationToken);
        return Ok(notifications);
    }

    /// <summary>Listar somente notificações não lidas.</summary>
    [HttpGet("unread")]
    [ProducesResponseType<IReadOnlyList<NotificationDto>>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> GetUnread(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var notifications = await service.GetUnreadAsync(userId, cancellationToken);
        return Ok(notifications);
    }

    /// <summary>Contagem de notificações não lidas (útil para badge no app).</summary>
    [HttpGet("unread/count")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> CountUnread(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var count = await service.CountUnreadAsync(userId, cancellationToken);
        return Ok(new { count });
    }

    /// <summary>Marcar uma notificação como lida.</summary>
    [HttpPatch("{notificationId:guid}/read")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> MarkAsRead(
        Guid notificationId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var updated = await service.MarkAsReadAsync(notificationId, userId, cancellationToken);
        return updated ? NoContent() : NotFoundProblem("Notificação não encontrada.");
    }

    /// <summary>Marcar todas as notificações como lidas.</summary>
    [HttpPatch("read-all")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> MarkAllAsRead(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var count = await service.MarkAllAsReadAsync(userId, cancellationToken);
        return Ok(new { markedAsRead = count });
    }

    /// <summary>Deletar uma notificação.</summary>
    [HttpDelete("{notificationId:guid}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(
        Guid notificationId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var deleted = await service.DeleteAsync(notificationId, userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Notificação não encontrada.");
    }
}
