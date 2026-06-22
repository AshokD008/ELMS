package com.lms.usermanagementservice.service.impl;

import com.lms.usermanagementservice.constant.LogConstants;
import com.lms.usermanagementservice.constant.MessageConstants;
import com.lms.usermanagementservice.dto.request.CreateStudentRequest;
import com.lms.usermanagementservice.dto.request.UpdateStudentRequest;
import com.lms.usermanagementservice.dto.response.PageResponse;
import com.lms.usermanagementservice.dto.response.StudentResponse;
import com.lms.usermanagementservice.entity.College;
import com.lms.usermanagementservice.entity.Role;
import com.lms.usermanagementservice.entity.StudentProfile;
import com.lms.usermanagementservice.entity.User;
import com.lms.usermanagementservice.entity.UserRole;
import com.lms.usermanagementservice.enums.UserRoleType;
import com.lms.usermanagementservice.enums.UserStatus;
import com.lms.usermanagementservice.exception.DuplicateResourceException;
import com.lms.usermanagementservice.exception.ForbiddenException;
import com.lms.usermanagementservice.exception.ResourceNotFoundException;
import com.lms.usermanagementservice.exception.ValidationException;
import com.lms.usermanagementservice.mapper.StudentMapper;
import com.lms.usermanagementservice.repository.CollegeRepository;
import com.lms.usermanagementservice.repository.RoleRepository;
import com.lms.usermanagementservice.repository.StudentProfileRepository;
import com.lms.usermanagementservice.repository.UserRepository;
import com.lms.usermanagementservice.repository.UserRoleRepository;
import com.lms.usermanagementservice.service.AuditLogService;
import com.lms.usermanagementservice.service.StudentService;
import com.lms.usermanagementservice.util.LogUtil;
import com.lms.usermanagementservice.util.PasswordUtil;
import com.lms.usermanagementservice.util.SecurityUtil;
import com.lms.usermanagementservice.validator.EmailValidator;
import com.lms.usermanagementservice.validator.PasswordValidator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

	private static final String STUDENT_CACHE_PREFIX = "STUDENT:";

	private final UserRepository userRepository;
	private final StudentProfileRepository studentProfileRepository;
	private final CollegeRepository collegeRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;

	private final StudentMapper studentMapper;

	private final PasswordUtil passwordUtil;
	private final SecurityUtil securityUtil;

	private final EmailValidator emailValidator;
	private final PasswordValidator passwordValidator;

	private final AuditLogService auditLogService;

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	@Transactional
	public StudentResponse createStudent(CreateStudentRequest request) {

		log.info(LogConstants.STUDENT_CREATE_INITIATED);

		validateCreateStudentRequest(request);

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new DuplicateResourceException(MessageConstants.EMAIL_ALREADY_EXISTS);
		}

		if (studentProfileRepository.existsByRollNumber(request.getRollNumber())) {
			throw new DuplicateResourceException(MessageConstants.ROLL_NUMBER_ALREADY_EXISTS);
		}

		College college = collegeRepository.findById(request.getCollegeId())
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.COLLEGE_NOT_FOUND));

		User user = new User();

		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setEmail(request.getEmail());
		user.setPhoneNumber(request.getPhoneNumber());
		user.setPassword(passwordUtil.encode(request.getPassword()));
		user.setStatus(UserStatus.ACTIVE);
		user.setEnabled(true);
		user.setIsDeleted(false);
		user.setEmailVerified(true);

		User savedUser = userRepository.save(user);

		Role studentRole = roleRepository.findByRoleName(UserRoleType.STUDENT.name())
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.ROLE_NOT_FOUND));

		UserRole userRole = new UserRole();

		userRole.setUser(savedUser);
		userRole.setRole(studentRole);

		userRoleRepository.save(userRole);

		StudentProfile profile = new StudentProfile();

		profile.setUser(savedUser);
		profile.setCollege(college);
		profile.setRollNumber(request.getRollNumber());
		profile.setDepartment(request.getDepartment());
		profile.setYear(request.getYear());
		profile.setSemester(request.getSemester());
		profile.setSection(request.getSection());
		profile.setAdmissionDate(LocalDateTime.now());

		StudentProfile savedProfile = studentProfileRepository.save(profile);

		cacheStudent(savedProfile);

		auditLogService.createAuditLog(savedUser.getId(), "STUDENT_CREATED", "Student created successfully");

		log.info(LogConstants.STUDENT_CREATE_SUCCESS);

		return studentMapper.toResponse(savedProfile);
	}

	@Override
	@Transactional
	public StudentResponse updateStudent(Long studentId, UpdateStudentRequest request) {

		StudentProfile studentProfile = studentProfileRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		validateStudentOwnership(studentProfile);

		User user = studentProfile.getUser();

		if (StringUtils.hasText(request.getFirstName())) {
			user.setFirstName(request.getFirstName());
		}

		if (StringUtils.hasText(request.getLastName())) {
			user.setLastName(request.getLastName());
		}

		if (StringUtils.hasText(request.getPhoneNumber())) {
			user.setPhoneNumber(request.getPhoneNumber());
		}

		if (StringUtils.hasText(request.getDepartment())) {
			studentProfile.setDepartment(request.getDepartment());
		}

		if (request.getYear() != null) {
			studentProfile.setYear(request.getYear());
		}

		if (request.getSemester() != null) {
			studentProfile.setSemester(request.getSemester());
		}

		if (StringUtils.hasText(request.getSection())) {
			studentProfile.setSection(request.getSection());
		}

		userRepository.save(user);

		StudentProfile updatedProfile = studentProfileRepository.save(studentProfile);

		cacheStudent(updatedProfile);

		auditLogService.createAuditLog(user.getId(), "STUDENT_UPDATED", "Student updated successfully");

		return studentMapper.toResponse(updatedProfile);
	}

	@Override
	@Transactional(readOnly = true)
	public StudentResponse getStudentById(Long studentId) {

		StudentProfile profile = studentProfileRepository.findWithUserAndCollegeById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));
		validateStudentOwnership(profile);
		return studentMapper.toResponse(profile);
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<StudentResponse> getAllStudents(int page, int size, String sortBy, String sortDirection) {

		Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<StudentProfile> studentPage = isSuperAdmin() ? studentProfileRepository.findAll(pageable)
				: studentProfileRepository.findAllByUserCollegeAdminId(SecurityUtil.getCurrentUserId(), pageable);

		List<StudentResponse> responses = studentPage.getContent().stream().map(studentMapper::toResponse).toList();

		return PageResponse.<StudentResponse>builder().content(responses).page(page).size(size)
				.totalElements(studentPage.getTotalElements()).totalPages(studentPage.getTotalPages())
				.last(studentPage.isLast()).build();
	}

	@Override
	@Transactional(readOnly = true)
	public PageResponse<StudentResponse> searchStudents(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		Specification<StudentProfile> specification = (root, query, criteriaBuilder) -> {

			List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.hasText(keyword)) {

				predicates.add(criteriaBuilder.or(
						criteriaBuilder.like(criteriaBuilder.lower(root.get("rollNumber")),
								"%" + keyword.toLowerCase() + "%"),
						criteriaBuilder.like(criteriaBuilder.lower(root.get("department")),
								"%" + keyword.toLowerCase() + "%")));
			}

			if (!isSuperAdmin()) {
				predicates.add(criteriaBuilder.equal(root.get("user").get("collegeAdmin").get("id"),
						SecurityUtil.getCurrentUserId()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};

		Page<StudentProfile> studentPage = studentProfileRepository.findAll(specification, pageable);

		List<StudentResponse> responses = studentPage.getContent().stream().map(studentMapper::toResponse).toList();

		return PageResponse.<StudentResponse>builder().content(responses).page(page).size(size)
				.totalPages(studentPage.getTotalPages()).totalElements(studentPage.getTotalElements())
				.last(studentPage.isLast()).build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<StudentResponse> getStudentsByCollege(Long collegeId) {

		List<StudentProfile> profiles = isSuperAdmin() ? studentProfileRepository.findAllByCollegeId(collegeId)
				: studentProfileRepository.findAllByCollegeIdAndUserCollegeAdmin_Id(collegeId,
						SecurityUtil.getCurrentUserId());

		return profiles.stream().map(studentMapper::toResponse).toList();
	}

	@Override
	@Transactional
	public StudentResponse activateStudent(Long studentId) {

		StudentProfile profile = studentProfileRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		profile.getUser().setStatus(UserStatus.ACTIVE);

		studentProfileRepository.save(profile);

		auditLogService.createAuditLog(profile.getUser().getId(), "STUDENT_ACTIVATED",
				"Student activated successfully");

		return studentMapper.toResponse(profile);
	}

	@Override
	@Transactional
	public StudentResponse deactivateStudent(Long studentId) {

		StudentProfile profile = studentProfileRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		profile.getUser().setStatus(UserStatus.REJECTED);

		studentProfileRepository.save(profile);

		auditLogService.createAuditLog(profile.getUser().getId(), "STUDENT_DEACTIVATED",
				"Student deactivated successfully");

		return studentMapper.toResponse(profile);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {

		return userRepository.existsByEmail(email);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByRollNumber(String rollNumber) {

		return studentProfileRepository.existsByRollNumber(rollNumber);
	}

	@Override
	@Transactional
	public void deleteStudent(Long studentId) {

		StudentProfile profile = studentProfileRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		validateStudentOwnership(profile);

		studentProfileRepository.delete(profile);

		userRepository.delete(profile.getUser());

		redisTemplate.delete(STUDENT_CACHE_PREFIX + studentId);

		auditLogService.createAuditLog(profile.getUser().getId(), "STUDENT_DELETED", "Student deleted permanently");
	}

	@Override
	@Transactional
	public void softDeleteStudent(Long studentId) {

		StudentProfile profile = studentProfileRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		User user = profile.getUser();

		user.setIsDeleted(true);
		user.setStatus(UserStatus.REJECTED);

		userRepository.save(user);

		redisTemplate.delete(STUDENT_CACHE_PREFIX + studentId);

		auditLogService.createAuditLog(user.getId(), "STUDENT_SOFT_DELETED", "Student soft deleted");
	}

	@Override
	@Transactional(readOnly = true)
	public StudentResponse getCurrentStudentProfile() {

		User currentUser = securityUtil.getCurrentUser();

		StudentProfile profile = studentProfileRepository.findByUserId(currentUser.getId())
				.orElseThrow(() -> new ResourceNotFoundException(MessageConstants.STUDENT_NOT_FOUND));

		return studentMapper.toResponse(profile);
	}

	private void validateCreateStudentRequest(CreateStudentRequest request) {

		if (!StringUtils.hasText(request.getFirstName())) {
			throw new ValidationException(MessageConstants.FIRST_NAME_REQUIRED);
		}

		if (!StringUtils.hasText(request.getEmail())) {
			throw new ValidationException(MessageConstants.EMAIL_REQUIRED);
		}

		if (!emailValidator.isValid(request.getEmail())) {
			throw new ValidationException(MessageConstants.INVALID_EMAIL);
		}

		passwordValidator.validate(request.getPassword());

		if (!StringUtils.hasText(request.getRollNumber())) {
			throw new ValidationException(MessageConstants.ROLL_NUMBER_REQUIRED);
		}

		if (request.getCollegeId() == null) {
			throw new ValidationException(MessageConstants.COLLEGE_ID_REQUIRED);
		}
	}

	private void validateStudentOwnership(StudentProfile studentProfile) {

		User currentUser = securityUtil.getCurrentUser();

		boolean isSuperAdmin = currentUser.getUserRoles().stream()
				.anyMatch(role -> isRole(role.getRole().getName(), UserRoleType.SUPER_ADMIN));

		boolean ownsStudent = studentProfile.getUser().getId().equals(currentUser.getId())
				|| currentUser.getId().equals(studentProfile.getUser().getCollegeAdminId());
		if (!isSuperAdmin && !ownsStudent) {

			throw new ForbiddenException(MessageConstants.ACCESS_DENIED);
		}
	}

	private void cacheStudent(StudentProfile profile) {

		redisTemplate.opsForValue().set(STUDENT_CACHE_PREFIX + profile.getId(), studentMapper.toResponse(profile));
	}

	private boolean isSuperAdmin() {
		User currentUser = securityUtil.getCurrentUser();
		return currentUser != null && currentUser.getUserRoles().stream()
				.anyMatch(role -> isRole(role.getRole().getName(), UserRoleType.SUPER_ADMIN));
	}

	private boolean isRole(String actualRole, UserRoleType expectedRole) {

		return expectedRole.name().equals(actualRole) || ("ROLE_" + expectedRole.name()).equals(actualRole);
	}
}
