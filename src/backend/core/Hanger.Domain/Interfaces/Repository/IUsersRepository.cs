namespace Domain.Interfaces;

public interface IUsersRepository {
    Task<Guid> CreateAsync(CreateUserRequest request);
    Task<UserRecord?> GetByIdAsync(Guid id);
    Task<UserRecord?> GetByEmailAsync(string email);
    Task<UserRecord?> GetByUsernameAsync(string username);
    Task<IReadOnlyList<UserRecord>> GetAllAsync(int limit = 50, int offset = 0);
    Task<bool> UpdateAsync(Guid id, UpdateUserRequest request);
    Task<bool> UpdatePartialAsync(Guid id, PatchUserRequest request);
    Task<bool> DeleteAsync(Guid id);
    Task<bool> ExistsAsync(Guid id);
    Task<bool> EmailExistsAsync(string email);
    Task<bool> UsernameExistsAsync(string username);
}