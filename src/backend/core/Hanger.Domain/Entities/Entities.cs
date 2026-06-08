namespace Hanger.Domain.Entities;

public sealed record User(
    Guid Id,
    string Username,
    string Email,
    string PasswordHash,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity,
    DateTime CreatedAt);

public sealed record Category(int Id, string Name);

public sealed record TypeEntity(int Id, int CategoryId, string Name);

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
    DateTime CreatedAt);

public sealed record PostTag(Guid PostId, int CategoryId, int? TypeId);

public sealed record Follow(Guid FollowerId, Guid FollowingId, DateTime CreatedAt);

public sealed record Like(Guid UserId, Guid PostId, DateTime CreatedAt);

public sealed record Comment(Guid Id, Guid PostId, Guid UserId, string Content, DateTime CreatedAt);

public sealed record Notification(
    Guid Id,
    Guid RecipientId,
    Guid SenderId,
    string Type,
    Guid? PostId,
    bool Read,
    DateTime CreatedAt);

public sealed record DeviceToken(
    Guid Id,
    Guid UserId,
    string Token,
    string Platform,
    DateTime UpdatedAt);

