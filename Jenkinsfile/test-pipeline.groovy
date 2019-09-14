pipeline {
	agent any
	stages {
		stage (one) {
			steps {
				echo "step 1"
			}
		}
		stage (two) {
			steps {
				echo "step 2"
			}
		}
	}
}
