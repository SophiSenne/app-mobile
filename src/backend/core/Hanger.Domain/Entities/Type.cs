namespace Hanger.Domain.Entities;

public sealed record TypeEntity(
    int Id, 
    int CategoryId, 
    string Name
);