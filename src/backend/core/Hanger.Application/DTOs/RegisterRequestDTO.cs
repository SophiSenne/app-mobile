namespace Hanger.Application.DTOs;

public sealed record RegisterRequest(
    string Username,
    string Email,
    string Password,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);