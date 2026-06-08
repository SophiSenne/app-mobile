using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class HangerRepository(NpgsqlDataSource dataSource) : IHangerRepository
{
    private const string PostSelect = """
        SELECT
            p.id,
            p.user_id,
            u.username,
            p.image_url,
            p.title,
            p.caption,
            p.weather_condition,
            p.temperature,
            p.city,
            p.share_count,
            p.created_at,
            pt.category_id,
            c.name AS category_name,
            pt.type_id,
            t.name AS type_name
        FROM posts p
        INNER JOIN users u ON u.id = p.user_id
        LEFT JOIN post_tags pt ON pt.post_id = p.id
        LEFT JOIN categories c ON c.id = pt.category_id
        LEFT JOIN types t ON t.id = pt.type_id
        """;

    public async Task<UserDto?> GetUserByIdAsync(Guid userId, CancellationToken cancellationToken)
    {
        const string sql = """
            SELECT id, username, email, bio, avatar_url, location_city, created_at
            FROM users
            WHERE id = @id
            """;

        await using var command = dataSource.CreateCommand(sql);
        command.Parameters.AddWithValue("id", userId);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        return await reader.ReadAsync(cancellationToken) ? ReadUser(reader) : null;
    }

    public async Task<(UserDto User, string PasswordHash)?> GetUserByEmailOrUsernameAsync(
        string emailOrUsername,
        CancellationToken cancellationToken)
    {
        const string sql = """
            SELECT id, username, email, password_hash, bio, avatar_url, location_city, created_at
            FROM users
            WHERE lower(email) = lower(@emailOrUsername)
               OR lower(username) = lower(@emailOrUsername)
            """;

        await using var command = dataSource.CreateCommand(sql);
        command.Parameters.AddWithValue("emailOrUsername", emailOrUsername);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        if (!await reader.ReadAsync(cancellationToken))
        {
            return null;
        }

        return (ReadUser(reader), reader.GetString(reader.GetOrdinal("password_hash")));
    }

    public async Task<UserDto> CreateUserAsync(
        RegisterRequest request,
        string passwordHash,
        CancellationToken cancellationToken)
    {
        const string sql = """
            INSERT INTO users (username, email, password_hash, bio, avatar_url, location_city)
            VALUES (@username, @email, @passwordHash, @bio, @avatarUrl, @locationCity)
            RETURNING id, username, email, bio, avatar_url, location_city, created_at
            """;

        await using var command = dataSource.CreateCommand(sql);
        command.Parameters.AddWithValue("username", request.Username.Trim());
        command.Parameters.AddWithValue("email", request.Email.Trim());
        command.Parameters.AddWithValue("passwordHash", passwordHash);
        command.Parameters.AddWithValue("bio", (object?)request.Bio ?? DBNull.Value);
        command.Parameters.AddWithValue("avatarUrl", (object?)request.AvatarUrl ?? DBNull.Value);
        command.Parameters.AddWithValue("locationCity", (object?)request.LocationCity ?? DBNull.Value);

        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        if (!await reader.ReadAsync(cancellationToken))
        {
            throw new InvalidOperationException("O banco de dados nao retornou o usuario criado.");
        }

        return ReadUser(reader);
    }

    public async Task<bool> DeleteUserAsync(Guid userId, CancellationToken cancellationToken)
    {
        await using var command = dataSource.CreateCommand("DELETE FROM users WHERE id = @id");
        command.Parameters.AddWithValue("id", userId);
        return await command.ExecuteNonQueryAsync(cancellationToken) > 0;
    }

    public async Task<IReadOnlyList<PostDto>> GetAllPostsAsync(CancellationToken cancellationToken) =>
        await ReadPostsAsync($"{PostSelect} ORDER BY p.created_at DESC", cancellationToken);

    public async Task<IReadOnlyList<PostDto>> GetPostsByUserAsync(Guid userId, CancellationToken cancellationToken)
    {
        await using var command = dataSource.CreateCommand($"{PostSelect} WHERE p.user_id = @userId ORDER BY p.created_at DESC");
        command.Parameters.AddWithValue("userId", userId);
        return await ReadPostsAsync(command, cancellationToken);
    }

    public async Task<IReadOnlyList<PostDto>> SearchPostsByTopicAsync(string topic, CancellationToken cancellationToken)
    {
        var like = $"%{topic}%";
        var sql = $"""
            {PostSelect}
            WHERE p.title ILIKE @topic
               OR p.caption ILIKE @topic
               OR p.city ILIKE @topic
               OR c.name ILIKE @topic
               OR t.name ILIKE @topic
            ORDER BY p.created_at DESC
            """;

        await using var command = dataSource.CreateCommand(sql);
        command.Parameters.AddWithValue("topic", like);
        return await ReadPostsAsync(command, cancellationToken);
    }

    public async Task<PostDto> CreatePostAsync(Guid userId, CreatePostRequest request, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);
        await using var transaction = await connection.BeginTransactionAsync(cancellationToken);

        var postId = await InsertPostAsync(connection, transaction, userId, request, cancellationToken);
        await UpsertPostTagAsync(connection, transaction, postId, request.CategoryId, request.TypeId, cancellationToken);
        var post = await GetPostByIdAsync(connection, transaction, postId, cancellationToken);

        await transaction.CommitAsync(cancellationToken);
        return post ?? throw new InvalidOperationException("O banco de dados nao retornou o post criado.");
    }

    public async Task<PostDto?> UpdatePostAsync(
        Guid postId,
        Guid userId,
        UpdatePostRequest request,
        CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);
        await using var transaction = await connection.BeginTransactionAsync(cancellationToken);

        var updated = await UpdatePostRowAsync(connection, transaction, postId, userId, request, cancellationToken);
        if (!updated)
        {
            await transaction.RollbackAsync(cancellationToken);
            return null;
        }

        await UpsertPostTagAsync(connection, transaction, postId, request.CategoryId, request.TypeId, cancellationToken);
        var post = await GetPostByIdAsync(connection, transaction, postId, cancellationToken);

        await transaction.CommitAsync(cancellationToken);
        return post;
    }

    public async Task<bool> DeletePostAsync(Guid postId, Guid userId, CancellationToken cancellationToken)
    {
        const string sql = "DELETE FROM posts WHERE id = @postId AND user_id = @userId";

        await using var command = dataSource.CreateCommand(sql);
        command.Parameters.AddWithValue("postId", postId);
        command.Parameters.AddWithValue("userId", userId);

        return await command.ExecuteNonQueryAsync(cancellationToken) > 0;
    }

    private static async Task<Guid> InsertPostAsync(
        NpgsqlConnection connection,
        NpgsqlTransaction transaction,
        Guid userId,
        CreatePostRequest request,
        CancellationToken cancellationToken)
    {
        const string sql = """
            INSERT INTO posts (user_id, image_url, title, caption, weather_condition, temperature, city)
            VALUES (@userId, @imageUrl, @title, @caption, @weatherCondition, @temperature, @city)
            RETURNING id
            """;

        await using var command = new NpgsqlCommand(sql, connection, transaction);
        command.Parameters.AddWithValue("userId", userId);
        command.Parameters.AddWithValue("imageUrl", request.ImageUrl.Trim());
        command.Parameters.AddWithValue("title", request.Title.Trim());
        command.Parameters.AddWithValue("caption", (object?)request.Caption ?? DBNull.Value);
        command.Parameters.AddWithValue("weatherCondition", (object?)request.WeatherCondition ?? DBNull.Value);
        command.Parameters.AddWithValue("temperature", (object?)request.Temperature ?? DBNull.Value);
        command.Parameters.AddWithValue("city", (object?)request.City ?? DBNull.Value);

        return (Guid)(await command.ExecuteScalarAsync(cancellationToken)
            ?? throw new InvalidOperationException("O banco de dados nao retornou o id do post."));
    }

    private static async Task<bool> UpdatePostRowAsync(
        NpgsqlConnection connection,
        NpgsqlTransaction transaction,
        Guid postId,
        Guid userId,
        UpdatePostRequest request,
        CancellationToken cancellationToken)
    {
        const string sql = """
            UPDATE posts
            SET image_url = COALESCE(@imageUrl, image_url),
                title = COALESCE(@title, title),
                caption = COALESCE(@caption, caption),
                weather_condition = COALESCE(@weatherCondition, weather_condition),
                temperature = COALESCE(@temperature, temperature),
                city = COALESCE(@city, city)
            WHERE id = @postId AND user_id = @userId
            """;

        await using var command = new NpgsqlCommand(sql, connection, transaction);
        command.Parameters.AddWithValue("postId", postId);
        command.Parameters.AddWithValue("userId", userId);
        command.Parameters.AddWithValue("imageUrl", (object?)request.ImageUrl?.Trim() ?? DBNull.Value);
        command.Parameters.AddWithValue("title", (object?)request.Title?.Trim() ?? DBNull.Value);
        command.Parameters.AddWithValue("caption", (object?)request.Caption ?? DBNull.Value);
        command.Parameters.AddWithValue("weatherCondition", (object?)request.WeatherCondition ?? DBNull.Value);
        command.Parameters.AddWithValue("temperature", (object?)request.Temperature ?? DBNull.Value);
        command.Parameters.AddWithValue("city", (object?)request.City ?? DBNull.Value);

        return await command.ExecuteNonQueryAsync(cancellationToken) > 0;
    }

    private static async Task UpsertPostTagAsync(
        NpgsqlConnection connection,
        NpgsqlTransaction transaction,
        Guid postId,
        int? categoryId,
        int? typeId,
        CancellationToken cancellationToken)
    {
        if (categoryId is null)
        {
            return;
        }

        const string sql = """
            INSERT INTO post_tags (post_id, category_id, type_id)
            VALUES (@postId, @categoryId, @typeId)
            ON CONFLICT (post_id, category_id)
            DO UPDATE SET type_id = EXCLUDED.type_id
            """;

        await using var command = new NpgsqlCommand(sql, connection, transaction);
        command.Parameters.AddWithValue("postId", postId);
        command.Parameters.AddWithValue("categoryId", categoryId.Value);
        command.Parameters.AddWithValue("typeId", (object?)typeId ?? DBNull.Value);

        await command.ExecuteNonQueryAsync(cancellationToken);
    }

    private async Task<PostDto?> GetPostByIdAsync(
        NpgsqlConnection connection,
        NpgsqlTransaction transaction,
        Guid postId,
        CancellationToken cancellationToken)
    {
        await using var command = new NpgsqlCommand($"{PostSelect} WHERE p.id = @postId", connection, transaction);
        command.Parameters.AddWithValue("postId", postId);

        var posts = await ReadPostsAsync(command, cancellationToken);
        return posts.FirstOrDefault();
    }

    private async Task<IReadOnlyList<PostDto>> ReadPostsAsync(string sql, CancellationToken cancellationToken)
    {
        await using var command = dataSource.CreateCommand(sql);
        return await ReadPostsAsync(command, cancellationToken);
    }

    private static async Task<IReadOnlyList<PostDto>> ReadPostsAsync(NpgsqlCommand command, CancellationToken cancellationToken)
    {
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        var posts = new List<PostDto>();

        while (await reader.ReadAsync(cancellationToken))
        {
            posts.Add(ReadPost(reader));
        }

        return posts;
    }

    private static UserDto ReadUser(NpgsqlDataReader reader) =>
        new(
            reader.GetGuid(reader.GetOrdinal("id")),
            reader.GetString(reader.GetOrdinal("username")),
            reader.GetString(reader.GetOrdinal("email")),
            ReadNullableString(reader, "bio"),
            ReadNullableString(reader, "avatar_url"),
            ReadNullableString(reader, "location_city"),
            reader.GetDateTime(reader.GetOrdinal("created_at")));

    private static PostDto ReadPost(NpgsqlDataReader reader) =>
        new(
            reader.GetGuid(reader.GetOrdinal("id")),
            reader.GetGuid(reader.GetOrdinal("user_id")),
            reader.GetString(reader.GetOrdinal("username")),
            reader.GetString(reader.GetOrdinal("image_url")),
            reader.GetString(reader.GetOrdinal("title")),
            ReadNullableString(reader, "caption"),
            ReadNullableString(reader, "weather_condition"),
            ReadNullableDouble(reader, "temperature"),
            ReadNullableString(reader, "city"),
            reader.GetInt32(reader.GetOrdinal("share_count")),
            reader.GetDateTime(reader.GetOrdinal("created_at")),
            ReadNullableInt(reader, "category_id"),
            ReadNullableString(reader, "category_name"),
            ReadNullableInt(reader, "type_id"),
            ReadNullableString(reader, "type_name"));

    private static string? ReadNullableString(NpgsqlDataReader reader, string name)
    {
        var ordinal = reader.GetOrdinal(name);
        return reader.IsDBNull(ordinal) ? null : reader.GetString(ordinal);
    }

    private static int? ReadNullableInt(NpgsqlDataReader reader, string name)
    {
        var ordinal = reader.GetOrdinal(name);
        return reader.IsDBNull(ordinal) ? null : reader.GetInt32(ordinal);
    }

    private static double? ReadNullableDouble(NpgsqlDataReader reader, string name)
    {
        var ordinal = reader.GetOrdinal(name);
        return reader.IsDBNull(ordinal) ? null : reader.GetDouble(ordinal);
    }
}
