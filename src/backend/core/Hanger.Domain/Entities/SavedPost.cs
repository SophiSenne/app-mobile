namespace Hanger.Domain.Entities;

public sealed record SavedPost(
    Guid UserId,
    Guid PostId,
    DateTime CreatedAt
);
