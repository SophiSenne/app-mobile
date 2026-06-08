namespace Hanger.Application.DTOs;

public sealed record RegisterRequest(
    string Username,
    string Email,
    string Password,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);

public sealed record LoginRequest(string EmailOrUsername, string Password);

public sealed record UserDto(
    Guid Id,
    string Username,
    string Email,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity,
    DateTime CreatedAt);

public sealed record AuthResponse(UserDto User);

public sealed record CreatePostRequest(
    string ImageUrl,
    string Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int? CategoryId,
    int? TypeId);

public sealed record UpdatePostRequest(
    string? ImageUrl,
    string? Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int? CategoryId,
    int? TypeId);

public sealed record PostDto(
    Guid Id,
    Guid UserId,
    string Username,
    string ImageUrl,
    string Title,
    string? Caption,
    string? WeatherCondition,
    double? Temperature,
    string? City,
    int ShareCount,
    DateTime CreatedAt,
    int? CategoryId,
    string? CategoryName,
    int? TypeId,
    string? TypeName);
