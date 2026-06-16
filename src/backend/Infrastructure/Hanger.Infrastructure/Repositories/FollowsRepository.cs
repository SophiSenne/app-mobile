using Dapper;
using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Npgsql;

namespace Hanger.Infrastructure.Repositories;

public sealed class FollowsRepository(NpgsqlDataSource dataSource) : IFollowsRepository
{
    public async Task<IReadOnlyList<FollowDto>> GetFollowingAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT
                f.follower_id, u1.username AS follower_username,
                f.following_id, u2.username AS following_username,
                f.created_at
            FROM follows f
            JOIN users u1 ON u1.id = f.follower_id
            JOIN users u2 ON u2.id = f.following_id
            WHERE f.follower_id = @UserId
            ORDER BY f.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<FollowDto>(sql, new { UserId = userId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<IReadOnlyList<FollowDto>> GetFollowersAsync(
        Guid userId, int limit, int offset, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT
                f.follower_id, u1.username AS follower_username,
                f.following_id, u2.username AS following_username,
                f.created_at
            FROM follows f
            JOIN users u1 ON u1.id = f.follower_id
            JOIN users u2 ON u2.id = f.following_id
            WHERE f.following_id = @UserId
            ORDER BY f.created_at DESC
            LIMIT @Limit OFFSET @Offset
            """;

        var result = await connection.QueryAsync<FollowDto>(sql, new { UserId = userId, Limit = limit, Offset = offset });
        return result.ToList();
    }

    public async Task<bool> ExistsAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT COUNT(1) FROM follows
            WHERE follower_id = @FollowerId AND following_id = @FollowingId
            """;

        var count = await connection.ExecuteScalarAsync<int>(sql, new { FollowerId = followerId, FollowingId = followingId });
        return count > 0;
    }

    public async Task<FollowDto> FollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken)
    {
        if (followerId == followingId)
            throw new InvalidOperationException("Um usuário não pode seguir a si mesmo.");

        if (await ExistsAsync(followerId, followingId, cancellationToken))
            throw new InvalidOperationException("Você já segue este usuário.");

        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            INSERT INTO follows (follower_id, following_id)
            VALUES (@FollowerId, @FollowingId)
            RETURNING follower_id, following_id, created_at
            """;

        const string selectSql = """
            SELECT
                f.follower_id, u1.username AS follower_username,
                f.following_id, u2.username AS following_username,
                f.created_at
            FROM follows f
            JOIN users u1 ON u1.id = f.follower_id
            JOIN users u2 ON u2.id = f.following_id
            WHERE f.follower_id = @FollowerId AND f.following_id = @FollowingId
            """;

        await connection.ExecuteAsync(sql, new { FollowerId = followerId, FollowingId = followingId });
        return await connection.QuerySingleAsync<FollowDto>(selectSql, new { FollowerId = followerId, FollowingId = followingId });
    }

    public async Task<bool> UnfollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            DELETE FROM follows
            WHERE follower_id = @FollowerId AND following_id = @FollowingId
            """;

        var affected = await connection.ExecuteAsync(sql, new { FollowerId = followerId, FollowingId = followingId });
        return affected > 0;
    }

    public async Task<(int Followers, int Following)> GetCountsAsync(Guid userId, CancellationToken cancellationToken)
    {
        await using var connection = await dataSource.OpenConnectionAsync(cancellationToken);

        const string sql = """
            SELECT
                (SELECT COUNT(*)::INT FROM follows WHERE following_id = @UserId) AS followers,
                (SELECT COUNT(*)::INT FROM follows WHERE follower_id  = @UserId) AS following
            """;

        var row = await connection.QuerySingleAsync<(int Followers, int Following)>(sql, new { UserId = userId });
        return row;
    }
}
