using Hanger.Application.DTOs;
using Hanger.Application.Services;
using Hanger.Domain.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("users")]
public class UsersController
{
    public readonly IUsersRepository _userRepository;

    public UsersController(IUsersRepository usersRepository){
        _userRepository = usersRepository;
    }

    /// <summary>Read meu usuario.</summary>
    [HttpGet("me")]
    [ProducesResponseType<UserDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var user = await _userRepository.GetMeAsync(userId, cancellationToken);
        return user is null ? NotFoundProblem("Usuario nao encontrado.") : Ok(user);
    }

    /// <summary>Atualizar meu usuario (substituicao completa).</summary>
    [HttpPut("me")]
    [ProducesResponseType<UserDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> UpdateMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] UpdateUserRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var updated = await service.UpdateUserAsync(userId, request, cancellationToken);
        return updated is null ? NotFoundProblem("Usuario nao encontrado.") : Ok(updated);
    }

    /// <summary>Atualizar campos especificos do meu usuario.</summary>
    [HttpPatch("me")]
    [ProducesResponseType<UserDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> PatchMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] PatchUserRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var updated = await service.PatchUserAsync(userId, request, cancellationToken);
        return updated is null ? NotFoundProblem("Usuario nao encontrado.") : Ok(updated);
    }

    /// <summary>Deletar conta.</summary>
    [HttpDelete("me")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var deleted = await service.DeleteAccountAsync(userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Usuario nao encontrado.");
    }

    // -------------------------------------------------------------------------
    // OUTROS USUARIOS
    // -------------------------------------------------------------------------

    /// <summary>Read usuario por id.</summary>
    [HttpGet("{userId:guid}")]
    [ProducesResponseType<UserDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(Guid userId, CancellationToken cancellationToken)
    {
        var user = await service.GetUserByIdAsync(userId, cancellationToken);
        return user is null ? NotFoundProblem("Usuario nao encontrado.") : Ok(user);
    }

    /// <summary>Read posts por usuario.</summary>
    [HttpGet("{userId:guid}/posts")]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetPostsByUser(Guid userId, CancellationToken cancellationToken)
    {
        var posts = await service.GetPostsByUserAsync(userId, cancellationToken);
        return Ok(posts);
    }
}