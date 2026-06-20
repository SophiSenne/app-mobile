namespace Hanger.Domain.Entities;

public sealed record Follow(
    Guid FollowerId, 
    Guid FollowingId, 
    DateTime CreatedAt
);
