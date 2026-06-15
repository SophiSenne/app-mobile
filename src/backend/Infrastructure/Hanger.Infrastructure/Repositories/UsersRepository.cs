using System.Data;
using Hanger.Infrastructure.Data;
using Hanger.Application.DTOs;
using Dapper;

namespace Hanger.Infrastructure.Repositories;

public class UserRepository : IUsersRepository
{
    private readonly IDbConnection _connection;
    private static readonly TableDefinition Table;

    static UserRepository()
    {
        var schema = new HangerSchema();
        schema.TryGetTable("users", out var table);
        Table = table;
    }

    public UserRepository(IDbConnection connection)
    {
        _connection = connection;
    }

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public async Task<Guid> CreateAsync(CreateUserRequest request)
    {
        // Insertable columns: username, email, password_hash, bio, avatar_url, location_city
        // (id and created_at are database-generated)
        const string sql = """
            INSERT INTO users (username, email, password_hash, bio, avatar_url, location_city)
            VALUES (@Username, @Email, @PasswordHash, @Bio, @AvatarUrl, @LocationCity)
            RETURNING id
            """;

        return await _connection.ExecuteScalarAsync<Guid>(sql, new
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
    // READ
    // -------------------------------------------------------------------------

    public async Task<UserRecord?> GetByIdAsync(Guid id)
    {
        const string sql = """
            SELECT id, username, email, password_hash, bio, avatar_url, location_city, created_at
            FROM users
            WHERE id = @Id
            """;

        return await _connection.QuerySingleOrDefaultAsync<UserRecord>(sql, new { Id = id });
    }

    public async Task<UserRecord?> GetByEmailAsync(string email)
    {
        const string sql = """
            SELECT id, username, email, password_hash, bio, avatar_url, location_city, created_at
            FROM users
            WHERE email = @Email
            """;

        return await _connection.QuerySingleOrDefaultAsync<UserRecord>(sql, new { Email = email });
    }

    public async Task<UserRecord?> GetByUsernameAsync(string username)
    {
        const string sql = """
            SELECT id, username, email, password_hash, bio, avatar_url, location_city, created_at
            FROM users
            WHERE username = @Username
            """;

        return await _connection.QuerySingleOrDefaultAsync<UserRecord>(sql, new { Username = username });
    }

    public async Task<IReadOnlyList<UserRecord>> GetAllAsync(int limit = 50, int offset = 0)
    {
        const string sql = """
            SELECT id, username, email, password_hash, bio, avatar_url, location_city, created_at
            FROM users
            ORDER BY created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await _connection.QueryAsync<UserRecord>(sql, new { Limit = limit, Offset = offset });
        return result.ToList();
    }

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public async Task<bool> UpdateAsync(Guid id, UpdateUserRequest request)
    {
        // Updatable columns: all except the primary key (id)
        // created_at is also database-generated, so excluded
        const string sql = """
            UPDATE users
            SET username      = @Username,
                email         = @Email,
                password_hash = @PasswordHash,
                bio           = @Bio,
                avatar_url    = @AvatarUrl,
                location_city = @LocationCity
            WHERE id = @Id
            """;

        var affected = await _connection.ExecuteAsync(sql, new
        {
            Id = id,
            request.Username,
            request.Email,
            request.PasswordHash,
            request.Bio,
            request.AvatarUrl,
            request.LocationCity
        });

        return affected > 0;
    }

    public async Task<bool> UpdatePartialAsync(Guid id, PatchUserRequest request)
    {
        var setClauses = new List<string>();
        var parameters = new DynamicParameters();
        parameters.Add("Id", id);

        if (request.Username is not null)
        {
            setClauses.Add("username = @Username");
            parameters.Add("Username", request.Username);
        }

        if (request.Email is not null)
        {
            setClauses.Add("email = @Email");
            parameters.Add("Email", request.Email);
        }

        if (request.PasswordHash is not null)
        {
            setClauses.Add("password_hash = @PasswordHash");
            parameters.Add("PasswordHash", request.PasswordHash);
        }

        if (request.Bio is not null)
        {
            setClauses.Add("bio = @Bio");
            parameters.Add("Bio", request.Bio);
        }

        if (request.AvatarUrl is not null)
        {
            setClauses.Add("avatar_url = @AvatarUrl");
            parameters.Add("AvatarUrl", request.AvatarUrl);
        }

        if (request.LocationCity is not null)
        {
            setClauses.Add("location_city = @LocationCity");
            parameters.Add("LocationCity", request.LocationCity);
        }

        if (setClauses.Count == 0)
            return false;

        var sql = $"UPDATE users SET {string.Join(", ", setClauses)} WHERE id = @Id";

        var affected = await _connection.ExecuteAsync(sql, parameters);
        return affected > 0;
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public async Task<bool> DeleteAsync(Guid id)
    {
        const string sql = "DELETE FROM users WHERE id = @Id";

        var affected = await _connection.ExecuteAsync(sql, new { Id = id });
        return affected > 0;
    }

    // -------------------------------------------------------------------------
    // EXISTS
    // -------------------------------------------------------------------------

    public async Task<bool> ExistsAsync(Guid id)
    {
        const string sql = "SELECT COUNT(1) FROM users WHERE id = @Id";
        var count = await _connection.ExecuteScalarAsync<int>(sql, new { Id = id });
        return count > 0;
    }

    public async Task<bool> EmailExistsAsync(string email)
    {
        const string sql = "SELECT COUNT(1) FROM users WHERE email = @Email";
        var count = await _connection.ExecuteScalarAsync<int>(sql, new { Email = email });
        return count > 0;
    }

    public async Task<bool> UsernameExistsAsync(string username)
    {
        const string sql = "SELECT COUNT(1) FROM users WHERE username = @Username";
        var count = await _connection.ExecuteScalarAsync<int>(sql, new { Username = username });
        return count > 0;
    }
}
