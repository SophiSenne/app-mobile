namespace Hanger.Domain.Entities;

public sealed record DeviceToken(
    Guid Id,
    Guid UserId,
    string Token,
    string Platform,
    DateTime UpdatedAt
);