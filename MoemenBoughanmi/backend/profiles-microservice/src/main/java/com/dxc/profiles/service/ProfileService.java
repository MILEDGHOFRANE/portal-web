package com.dxc.profiles.service;

import com.dxc.profiles.entity.Profile;
import com.dxc.profiles.repository.ProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class ProfileService {
    
    @Autowired
    private ProfileRepository profileRepository;

    public Profile createProfile(Profile profile) {
        log.info("Creating profile for user: {}", profile.getUserId());
        return profileRepository.save(profile);
    }

    public Optional<Profile> getProfileByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    public Optional<Profile> getProfileByEmail(String email) {
        return profileRepository.findByEmail(email);
    }

    public Profile updateProfile(Long userId, Profile updates) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        
        if (updates.getFirstName() != null) profile.setFirstName(updates.getFirstName());
        if (updates.getLastName() != null) profile.setLastName(updates.getLastName());
        if (updates.getContactPhone() != null) profile.setContactPhone(updates.getContactPhone());
        if (updates.getResidentialAddress() != null) profile.setResidentialAddress(updates.getResidentialAddress());
        if (updates.getFamilyStatus() != null) profile.setFamilyStatus(updates.getFamilyStatus());
        if (updates.getTechnicalSkills() != null) profile.setTechnicalSkills(updates.getTechnicalSkills());
        if (updates.getNationalId() != null) profile.setNationalId(updates.getNationalId());
        if (updates.getProfessionalTitle() != null) profile.setProfessionalTitle(updates.getProfessionalTitle());
        if (updates.getDepartment() != null) profile.setDepartment(updates.getDepartment());
        
        return profileRepository.save(profile);
    }

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public void deleteProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        profileRepository.delete(profile);
    }
}
