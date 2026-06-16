using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;

namespace Hanger.Application.Services;

public sealed class CategoriesService(ICategoriesRepository repository) : ICategoriesService
{
    public Task<IReadOnlyList<CategoryDto>> GetAllAsync(CancellationToken cancellationToken) =>
        repository.GetAllAsync(cancellationToken);

    public Task<CategoryDto?> GetByIdAsync(int id, CancellationToken cancellationToken) =>
        repository.GetByIdAsync(id, cancellationToken);

    public Task<IReadOnlyList<TypeDto>> GetTypesByCategoryAsync(int categoryId, CancellationToken cancellationToken) =>
        repository.GetTypesByCategoryAsync(categoryId, cancellationToken);

    public Task<TypeDto?> GetTypeByIdAsync(int typeId, CancellationToken cancellationToken) =>
        repository.GetTypeByIdAsync(typeId, cancellationToken);

    public async Task<CategoryDto> CreateCategoryAsync(CreateCategoryRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new InvalidOperationException("O nome da categoria é obrigatório.");

        return await repository.CreateCategoryAsync(request, cancellationToken);
    }

    public async Task<TypeDto> CreateTypeAsync(int categoryId, CreateTypeRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new InvalidOperationException("O nome do tipo é obrigatório.");

        var categoryExists = await repository.GetByIdAsync(categoryId, cancellationToken);
        if (categoryExists is null)
            throw new KeyNotFoundException("Categoria não encontrada.");

        // Garante que o categoryId da rota prevalece
        var adjusted = request with { CategoryId = categoryId };
        return await repository.CreateTypeAsync(adjusted, cancellationToken);
    }

    public async Task<CategoryDto?> UpdateCategoryAsync(int id, UpdateCategoryRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new InvalidOperationException("O nome da categoria é obrigatório.");

        return await repository.UpdateCategoryAsync(id, request, cancellationToken);
    }

    public async Task<TypeDto?> UpdateTypeAsync(int typeId, UpdateTypeRequest request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.Name))
            throw new InvalidOperationException("O nome do tipo é obrigatório.");

        return await repository.UpdateTypeAsync(typeId, request, cancellationToken);
    }

    public Task<bool> DeleteCategoryAsync(int id, CancellationToken cancellationToken) =>
        repository.DeleteCategoryAsync(id, cancellationToken);

    public Task<bool> DeleteTypeAsync(int typeId, CancellationToken cancellationToken) =>
        repository.DeleteTypeAsync(typeId, cancellationToken);
}
