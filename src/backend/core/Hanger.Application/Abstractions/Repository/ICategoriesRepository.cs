using Hanger.Application.DTOs;

namespace Hanger.Application.Abstractions;

public interface ICategoriesRepository
{
    Task<IReadOnlyList<CategoryDto>> GetAllAsync(CancellationToken cancellationToken);

    Task<CategoryDto?> GetByIdAsync(int id, CancellationToken cancellationToken);

    Task<IReadOnlyList<TypeDto>> GetTypesByCategoryAsync(int categoryId, CancellationToken cancellationToken);

    Task<TypeDto?> GetTypeByIdAsync(int typeId, CancellationToken cancellationToken);

    Task<CategoryDto> CreateCategoryAsync(CreateCategoryRequest request, CancellationToken cancellationToken);

    Task<TypeDto> CreateTypeAsync(CreateTypeRequest request, CancellationToken cancellationToken);

    Task<CategoryDto?> UpdateCategoryAsync(int id, UpdateCategoryRequest request, CancellationToken cancellationToken);

    Task<TypeDto?> UpdateTypeAsync(int typeId, UpdateTypeRequest request, CancellationToken cancellationToken);

    Task<bool> DeleteCategoryAsync(int id, CancellationToken cancellationToken);

    Task<bool> DeleteTypeAsync(int typeId, CancellationToken cancellationToken);
}
