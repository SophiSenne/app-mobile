namespace Hanger.Application.DTOs;

public sealed record User(
    Guid Id,
    string Username,
    string Email,
    string PasswordHash,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity,
    DateTime CreatedAt);

public sealed record CreateUserRequest(
    string Username,
    string Email,
    string PasswordHash,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);

public sealed record UpdateUserRequest(
    string Username,
    string Email,
    string PasswordHash,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);

public sealed record PatchUserRequest(
    string? Username = null,
    string? Email = null,
    string? PasswordHash = null,
    string? Bio = null,
    string? AvatarUrl = null,
    string? LocationCity = null);