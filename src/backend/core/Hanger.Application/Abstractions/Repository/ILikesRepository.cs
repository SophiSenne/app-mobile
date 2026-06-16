using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ILikesRepository
{
    /// <summary>Lista os likes de um post.</summary>
    Task<IReadOnlyList<LikeDto>> GetByPostIdAsync(Guid postId, int limit, int offset, CancellationToken cancellationToken);

    /// <summary>Lista os posts curtidos por um usuário.</summary>
    Task<IReadOnlyList<LikeDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    /// <summary>Retorna true se o usuário já curtiu o post.</summary>
    Task<bool> ExistsAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Curte um post. Lança <see cref="InvalidOperationException"/> se já curtiu.</summary>
    Task<LikeDto> LikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Remove o like. Retorna false se não existia.</summary>
    Task<bool> UnlikeAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Contagem de likes de um post.</summary>
    Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken);
}
