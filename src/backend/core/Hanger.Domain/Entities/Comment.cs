namespace Hanger.Domain.Entities;

public sealed record Comment(
    Guid Id, 
    Guid PostId, 
    Guid UserId, 
    string Content, 
    DateTime CreatedAt
);
