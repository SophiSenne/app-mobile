namespace Hanger.Domain.Entities;

public sealed record Like(
    Guid UserId, 
    Guid PostId, 
    DateTime CreatedAt
);
