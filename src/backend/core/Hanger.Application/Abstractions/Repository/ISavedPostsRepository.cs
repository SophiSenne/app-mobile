using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ISavedPostsRepository
{
    /// <summary>Lista os posts salvos por um usuário.</summary>
    Task<IReadOnlyList<PostDto>> GetByUserIdAsync(Guid userId, int limit, int offset, CancellationToken cancellationToken);

    /// <summary>Retorna true se o usuário já salvou o post.</summary>
    Task<bool> ExistsAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Salva um post. Lança <see cref="InvalidOperationException"/> se já foi salvo.</summary>
    Task<SavedPostDto> SaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Remove o post salvo. Retorna false se não existia.</summary>
    Task<bool> UnsaveAsync(Guid userId, Guid postId, CancellationToken cancellationToken);

    /// <summary>Contagem de posts salvos por um usuário.</summary>
    Task<int> CountByUserIdAsync(Guid userId, CancellationToken cancellationToken);
}
