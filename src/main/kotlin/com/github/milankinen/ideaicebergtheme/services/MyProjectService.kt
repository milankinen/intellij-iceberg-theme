package com.github.milankinen.ideaicebergtheme.services

import com.intellij.openapi.project.Project
import com.github.milankinen.ideaicebergtheme.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
