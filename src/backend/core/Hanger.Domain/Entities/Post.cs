namespace Hanger.Domain.Entities;

public sealed record Post(
    Guid Id,
    Guid UserId,
    string ImageUrl,
    string Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int ShareCount,
    DateTime CreatedAt
);