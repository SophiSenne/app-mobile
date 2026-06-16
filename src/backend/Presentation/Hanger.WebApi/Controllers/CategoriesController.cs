using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("categories")]
public class CategoriesController(ICategoriesService service) : ApiControllerBase
{
    // -------------------------------------------------------------------------
    // CATEGORIES
    // -------------------------------------------------------------------------

    /// <summary>Listar todas as categorias com seus tipos.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<CategoryDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAll(CancellationToken cancellationToken)
    {
        var categories = await service.GetAllAsync(cancellationToken);
        return Ok(categories);
    }

    /// <summary>Buscar categoria por id (inclui tipos relacionados).</summary>
    [HttpGet("{id:int}")]
    [ProducesResponseType<CategoryDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(int id, CancellationToken cancellationToken)
    {
        var category = await service.GetByIdAsync(id, cancellationToken);
        return category is null ? NotFoundProblem("Categoria não encontrada.") : Ok(category);
    }

    /// <summary>Criar nova categoria.</summary>
    [HttpPost]
    [ProducesResponseType<CategoryDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Create(
        [FromBody] CreateCategoryRequest request,
        CancellationToken cancellationToken)
    {
        try
        {
            var category = await service.CreateCategoryAsync(request, cancellationToken);
            return CreatedAtAction(nameof(GetById), new { id = category.Id }, category);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Atualizar nome de uma categoria.</summary>
    [HttpPut("{id:int}")]
    [ProducesResponseType<CategoryDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(
        int id,
        [FromBody] UpdateCategoryRequest request,
        CancellationToken cancellationToken)
    {
        try
        {
            var updated = await service.UpdateCategoryAsync(id, request, cancellationToken);
            return updated is null ? NotFoundProblem("Categoria não encontrada.") : Ok(updated);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Deletar categoria (cascade nos tipos relacionados).</summary>
    [HttpDelete("{id:int}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(int id, CancellationToken cancellationToken)
    {
        var deleted = await service.DeleteCategoryAsync(id, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Categoria não encontrada.");
    }

    // -------------------------------------------------------------------------
    // TYPES (sub-recurso)
    // -------------------------------------------------------------------------

    /// <summary>Listar tipos de uma categoria.</summary>
    [HttpGet("{categoryId:int}/types")]
    [ProducesResponseType<IReadOnlyList<TypeDto>>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetTypes(int categoryId, CancellationToken cancellationToken)
    {
        var category = await service.GetByIdAsync(categoryId, cancellationToken);
        if (category is null) return NotFoundProblem("Categoria não encontrada.");

        var types = await service.GetTypesByCategoryAsync(categoryId, cancellationToken);
        return Ok(types);
    }

    /// <summary>Buscar tipo por id.</summary>
    [HttpGet("types/{typeId:int}")]
    [ProducesResponseType<TypeDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetTypeById(int typeId, CancellationToken cancellationToken)
    {
        var type = await service.GetTypeByIdAsync(typeId, cancellationToken);
        return type is null ? NotFoundProblem("Tipo não encontrado.") : Ok(type);
    }

    /// <summary>Criar novo tipo dentro de uma categoria.</summary>
    [HttpPost("{categoryId:int}/types")]
    [ProducesResponseType<TypeDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> CreateType(
        int categoryId,
        [FromBody] CreateTypeRequest request,
        CancellationToken cancellationToken)
    {
        try
        {
            var type = await service.CreateTypeAsync(categoryId, request, cancellationToken);
            return CreatedAtAction(nameof(GetTypeById), new { typeId = type.Id }, type);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFoundProblem(ex.Message);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Atualizar nome de um tipo.</summary>
    [HttpPut("types/{typeId:int}")]
    [ProducesResponseType<TypeDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> UpdateType(
        int typeId,
        [FromBody] UpdateTypeRequest request,
        CancellationToken cancellationToken)
    {
        try
        {
            var updated = await service.UpdateTypeAsync(typeId, request, cancellationToken);
            return updated is null ? NotFoundProblem("Tipo não encontrado.") : Ok(updated);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Deletar tipo.</summary>
    [HttpDelete("types/{typeId:int}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteType(int typeId, CancellationToken cancellationToken)
    {
        var deleted = await service.DeleteTypeAsync(typeId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Tipo não encontrado.");
    }
}
