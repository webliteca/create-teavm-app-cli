const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 8080;

// Load the TeaVM-compiled application
const appPath = path.join(__dirname, 'app.js');
if (fs.existsSync(appPath)) {
    require(appPath);
} else {
    console.error('app.js not found at', appPath);
    console.error('Build the project first: ./mvnw -pl {{APP_NAME}}-api package -Pteavm');
    process.exit(1);
}
