namespace Hanger.Domain.Entities;

public sealed record Notification(
    Guid Id,
    Guid RecipientId,
    Guid SenderId,
    string Type,
    Guid? PostId,
    bool Read,
    DateTime CreatedAt
);


