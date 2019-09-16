//#!/usr/bin/env groovy
//@Library('jenkinsSharedLibrary_Dev')_
//import hudson.plugins.git.*;

def batches

pipeline {
    agent any
    //agent { label 'mgmt-node' }
    parameters {
        string(name: 'nodelist', defaultValue: '', description: '')
        string(name: 'deploymentBatches', defaultValue: '', description: '')
        string(name: 'folderName', defaultValue: '', description: '')
    }
    stages {
        stage("Git Checkout") {
            steps {
                checkout scm
            }
        }
        stage('Initialize') {
            // Set up List<Map<String,Closure>> describing the builds
            // git branch: 'dev',
            //     credentialsId: '',
            //     url: 'git@github.com:infacloud/iics-platform-jenkins-pipeline-library.git'
            steps {
                script{ 
                    println "Nodelist is ${params.nodelist}"
                    println "Deployment Batch list is ${params.deploymentBatches}"
                    println "Folder Name is ${params.folderName}"

                    if ("".equals(params.folderName) ) {
                        currentBuild.result = 'ABORTED'
                        error('folderName parameter is mandatory')
                    }

                    loadNodeList(params.nodelist)
                    getAllNodesAndCopyResources(params.nodelist,params.deploymentBatches)

                    batches=prepareDeploymentBatches(params.folderName, params.nodelist, params.deploymentBatches, "")
                    println batches
                    println("Initialized pipeline.")
                }
            }
        }
        
        stage('Run Stage') {
            steps {
                script{ 
                    batchNameList = []
                    batches.each { batchName, batchValue ->
                        batchNameList.add(batchName.substring(0,batchName.lastIndexOf("_")))
                    }
                    batchNameList = batchNameList.unique()
                    print batchNameList
                    
                    for (batchNameValue in batchNameList) {
                        parallelBatchMap = [:]
                        batches.each { batchName, batchValue ->
                            if (batchName.startsWith(batchNameValue)) {
                                for (builds in batchValue) {
                                    builds.each { buildName, buildValue ->
                                        parallelBatchMap.put(buildName,buildValue)
                                    }
                                }
                            }
                        }
                        print parallelBatchMap
                        stage(batchNameValue) {
                            parallel(parallelBatchMap)
                        }
                    }
                }
            }
        }

        stage('Finish') {
            steps {
                println('Build complete.')
            }
        }
    }
}
    