package com.codinglegion.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for Git operations in Coding Legion
 * Handles branch detection and changed file retrieval
 */
public class GitBranchChecker {
    
    private static final Set<String> PROTECTED_BRANCHES = new HashSet<>(Arrays.asList(
        "master", "main", "develop", "development"
    ));
    
    /**
     * Check if the current branch is a feature branch (not main/master)
     */
    public static boolean isFeatureBranch(Project project) {
        try {
            GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
            GitRepository repository = repositoryManager.getRepositoryForRootQuick(project.getBaseDir());
            
            if (repository == null) {
                return false;
            }
            
            String currentBranch = repository.getCurrentBranchName();
            if (currentBranch == null) {
                return false;
            }
            
            // Check if current branch is NOT in protected branches
            return !PROTECTED_BRANCHES.contains(currentBranch.toLowerCase());
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the current branch name
     */
    public static String getCurrentBranchName(Project project) {
        try {
            GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
            GitRepository repository = repositoryManager.getRepositoryForRootQuick(project.getBaseDir());
            
            if (repository == null) {
                return "unknown";
            }
            
            String branchName = repository.getCurrentBranchName();
            return branchName != null ? branchName : "unknown";
            
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    /**
     * Get all changed/added Java files in the current branch
     * Only returns .java files
     */
    public static List<VirtualFile> getChangedJavaFiles(Project project) {
        List<VirtualFile> changedFiles = new ArrayList<>();
        
        try {
            ChangeListManager changeListManager = ChangeListManager.getInstance(project);
            Collection<Change> changes = changeListManager.getAllChanges();
            
            for (Change change : changes) {
                VirtualFile file = null;
                
                // Get the file from the change
                if (change.getAfterRevision() != null) {
                    file = change.getAfterRevision().getFile().getVirtualFile();
                } else if (change.getBeforeRevision() != null) {
                    file = change.getBeforeRevision().getFile().getVirtualFile();
                }
                
                // Only include Java files
                if (file != null && file.getName().endsWith(".java")) {
                    changedFiles.add(file);
                }
            }
            
        } catch (Exception e) {
            // Return empty list if error occurs
        }
        
        return changedFiles;
    }
    
    /**
     * Check if there are any changed Java files
     */
    public static boolean hasChangedJavaFiles(Project project) {
        return !getChangedJavaFiles(project).isEmpty();
    }
    
    /**
     * Get summary of changed files
     */
    public static String getChangedFilesSummary(Project project) {
        List<VirtualFile> files = getChangedJavaFiles(project);
        
        if (files.isEmpty()) {
            return "No changed Java files";
        }
        
        return files.size() + " changed Java file(s):\n" + 
               files.stream()
                    .map(VirtualFile::getName)
                    .collect(Collectors.joining("\n"));
    }
}

