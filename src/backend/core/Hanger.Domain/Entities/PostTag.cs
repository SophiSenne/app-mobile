namespace Hanger.Domain.Entities;

public sealed record PostTag(
    Guid PostId, 
    int CategoryId, 
    int? TypeId
);