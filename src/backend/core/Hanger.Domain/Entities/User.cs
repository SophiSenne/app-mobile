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