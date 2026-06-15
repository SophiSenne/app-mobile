using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class UsersRepository(NpgsqlDataSource dataSource) : IUsersRepository
{
    // -------------------------------------------------------------------------
    // READ
    // -------------------------------------------------------------------------

    public async Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at
            FROM users
            WHERE id = @Id
            """;

        return await connection.QuerySingleOrDefaultAsync<UserDto>(sql, new { Id = userId });
    }

    public async Task<UserDto?> GetUserByEmailAsync(string email, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at
            FROM users
            WHERE email = @Email
            """;

        return await connection.QuerySingleOrDefaultAsync<UserDto>(sql, new { Email = email });
    }

    public async Task<UserDto?> GetUserByUsernameAsync(string username, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at
            FROM users
            WHERE username = @Username
            """;

        return await connection.QuerySingleOrDefaultAsync<UserDto>(sql, new { Username = username });
    }

    public async Task<IReadOnlyList<UserDto>> GetAllAsync(int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at
            FROM users
            ORDER BY created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<UserDto>(sql, new { Limit = limit, Offset = offset });
        return result.ToList();
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public async Task<UserDto> CreateAsync(CreateUserRequest request, CancellationToken cancellationToken)
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
            request.PasswordHash,
            request.Bio,
            request.AvatarUrl,
            request.LocationCity
        });
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public async Task<UserDto?> UpdateAsync(Guid id, UpdateUserRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            UPDATE users
            SET username      = @Username,
                email         = @Email,
                bio           = @Bio,
                avatar_url    = @AvatarUrl,
                location_city = @LocationCity
            WHERE id = @Id
            RETURNING id, username, email, bio, avatar_url, location_city, created_at
            """;

        return await connection.QuerySingleOrDefaultAsync<UserDto>(sql, new
        {
            Id = id,
            request.Username,
            request.Email,
            request.Bio,
            request.AvatarUrl,
            request.LocationCity
        });
    }

    public async Task<UserDto?> UpdatePartialAsync(Guid id, PatchUserRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        var setClauses = new List<string>();
        var parameters = new DynamicParameters();
        parameters.Add("Id", id);

        if (request.Username is not null)   { setClauses.Add("username = @Username");         parameters.Add("Username", request.Username); }
        if (request.Email is not null)      { setClauses.Add("email = @Email");               parameters.Add("Email", request.Email); }
        if (request.Bio is not null)        { setClauses.Add("bio = @Bio");                   parameters.Add("Bio", request.Bio); }
        if (request.AvatarUrl is not null)  { setClauses.Add("avatar_url = @AvatarUrl");      parameters.Add("AvatarUrl", request.AvatarUrl); }
        if (request.LocationCity is not null) { setClauses.Add("location_city = @LocationCity"); parameters.Add("LocationCity", request.LocationCity); }

        if (setClauses.Count == 0)
            return await GetUserByIdAsync(id, cancellationToken);

        var sql = $"""
            UPDATE users
            SET {string.Join(", ", setClauses)}
            WHERE id = @Id
            RETURNING id, username, email, bio, avatar_url, location_city, created_at
            """;

        return await connection.QuerySingleOrDefaultAsync<UserDto>(sql, parameters);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public async Task<bool> DeleteAsync(Guid id, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "DELETE FROM users WHERE id = @Id";
        var affected = await connection.ExecuteAsync(sql, new { Id = id });
        return affected > 0;
    }

    // -------------------------------------------------------------------------
    // EXISTS
    // -------------------------------------------------------------------------

    public async Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = "SELECT COUNT(1) FROM users WHERE id = @Id";
        var count = await connection.ExecuteScalarAsync<int>(sql, new { Id = id });
        return count > 0;
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
}