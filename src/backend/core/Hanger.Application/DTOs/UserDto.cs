namespace Hanger.Application.DTOs;

/// <summary>Dado retornado ao cliente — sem password_hash.</summary>
public sealed record UserDto(
    Guid Id,
    string Username,
    string Email,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity,
    DateTime CreatedAt);

/// <summary>Payload para criar um usuário (usado internamente pelo repositório).</summary>
public sealed record CreateUserRequest(
    string Username,
    string Email,
    string PasswordHash,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);

/// <summary>Substituição completa dos campos editáveis do usuário.</summary>
public sealed record UpdateUserRequest(
    string Username,
    string Email,
    string? Bio,
    string? AvatarUrl,
    string? LocationCity);

/// <summary>Atualização parcial — somente os campos não-nulos são aplicados.</summary>
public sealed record PatchUserRequest(
    string? Username = null,
    string? Email = null,
    string? Bio = null,
    string? AvatarUrl = null,
    string? LocationCity = null);