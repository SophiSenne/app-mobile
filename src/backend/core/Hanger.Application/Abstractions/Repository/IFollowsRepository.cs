using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface IFollowsRepository
{
    /// <summary>Lista os usuários que <paramref name="userId"/> segue.</summary>
    Task<IReadOnlyList<FollowDto>> GetFollowingAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    /// <summary>Lista os seguidores de <paramref name="userId"/>.</summary>
    Task<IReadOnlyList<FollowDto>> GetFollowersAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    /// <summary>Retorna true se <paramref name="followerId"/> já segue <paramref name="followingId"/>.</summary>
    Task<bool> ExistsAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);

    /// <summary>Cria o vínculo de follow. Lança <see cref="InvalidOperationException"/> em auto-follow ou duplicata.</summary>
    Task<FollowDto> FollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);

    /// <summary>Remove o vínculo. Retorna false se não existia.</summary>
    Task<bool> UnfollowAsync(Guid followerId, Guid followingId, CancellationToken cancellationToken);

    /// <summary>Contagem de seguidores e seguindo.</summary>
    Task<(int Followers, int Following)> GetCountsAsync(Guid userId, CancellationToken cancellationToken);
}
