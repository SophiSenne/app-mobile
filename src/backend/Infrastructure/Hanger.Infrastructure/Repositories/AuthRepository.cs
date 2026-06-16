using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class AuthRepository(NpgsqlDataSource dataSource) : IAuthRepository
{
    public async Task<(UserDto User, string PasswordHash)?> GetCredentialsByEmailOrUsernameAsync(
        string emailOrUsername,
        CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at, password_hash
            FROM users
            WHERE email = @Value OR username = @Value
            LIMIT 1
            """;

        var row = await connection.QuerySingleOrDefaultAsync<UserWithHash>(sql, new { Value = emailOrUsername });

        if (row is null)
            return null;

        var dto = new UserDto(row.Id, row.Username, row.Email, row.Bio, row.AvatarUrl, row.LocationCity, row.CreatedAt);
        return (dto, row.PasswordHash);
    }

    public async Task<UserDto> CreateUserAsync(
        RegisterRequest request,
        string passwordHash,
        CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            INSERT INTO users (username, email, password_hash, bio, avatar_url, location_city)
            VALUES (@Username, @Email, @PasswordHash, @Bio, @AvatarUrl, @LocationCity)
            RETURNING id, username, email, bio, avatar_url, location_city, created_at
            """;

        return await connection.QuerySingleAsync<UserDto>(sql, new
        {
            request.Username,
            request.Email,
            PasswordHash = passwordHash,
            request.Bio,
            request.AvatarUrl,
            request.LocationCity
        });
    }

    public async Task<bool> EmailExistsAsync(string email, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM users WHERE email = @Email";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { Email = email });
        return count > 0;
    }

    public async Task<bool> UsernameExistsAsync(string username, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM users WHERE username = @Username";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { Username = username });
        return count > 0;
    }

    private sealed record UserWithHash(
        Guid Id,
        string Username,
        string Email,
        string? Bio,
        string? AvatarUrl,
        string? LocationCity,
        DateTime CreatedAt,
        string PasswordHash);
}