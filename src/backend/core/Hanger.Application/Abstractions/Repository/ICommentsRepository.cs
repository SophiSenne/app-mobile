using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ICommentsRepository
{
    Task<IReadOnlyList<CommentDto>> GetByPostIdAsync(Guid postId, int limit, int offset, CancellationToken cancellationToken);

    Task<CommentDto?> GetByIdAsync(Guid commentId, CancellationToken cancellationToken);

    Task<CommentDto> CreateAsync(Guid postId, Guid userId, CreateCommentRequest request, CancellationToken cancellationToken);

    /// <summary>Atualiza o conteúdo. Só o autor pode editar (userId é verificado).</summary>
    Task<CommentDto?> UpdateAsync(Guid commentId, Guid userId, UpdateCommentRequest request, CancellationToken cancellationToken);

    /// <summary>Remove o comentário. Retorna false se não encontrado ou sem permissão.</summary>
    Task<bool> DeleteAsync(Guid commentId, Guid userId, CancellationToken cancellationToken);

    Task<int> CountByPostIdAsync(Guid postId, CancellationToken cancellationToken);
}
