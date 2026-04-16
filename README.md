# GreenChain

GreenChain is a comprehensive supply chain sustainability platform that helps businesses track, analyze, and optimize their environmental impact.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Data Model](#data-model)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Features
- **Core Indicators**: Monitor key sustainability metrics and carbon emissions data
- **Carbon Emission Charts**: Visualize emissions trends and transportation mode distribution
- **Supplier Chain Map**: Interactive global map of suppliers and shipment routes
- **Shipment Tracking**: Track and manage your supply chain shipments
- **Supplier Management**: Manage supplier information and certifications
- **Generate Reports**: Export sustainability data and charts to PDF

### Additional Features
- **User Authentication**: Secure login and user management
- **Interactive Globe**: 3D visualization of global supply chain
- **Responsive Design**: Optimized for desktop and mobile devices
- **Real-time Data**: Live updates and data refresh capabilities

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.2.4
- Spring Security
- Spring Data JPA
- MySQL
- Maven

### Frontend
- Vue 3.5.30
- Vue Router 4.6.4
- ECharts 5.6.0
- Globe.gl 2.45.3
- Three.js 0.183.2
- jsPDF 4.2.1
- html2canvas 1.4.1
- Vite 8.0.1

## Project Structure

```
GreenChain/
├── .github/             # GitHub workflows
├── frontend/            # Vue.js frontend
│   ├── dist/            # Build output
│   ├── public/          # Public assets
│   ├── src/             # Source code
│   │   ├── assets/      # Static assets
│   │   ├── components/  # Vue components
│   │   ├── composables/ # Vue composables
│   │   ├── layouts/     # Layout components
│   │   ├── router/      # Vue Router configuration
│   │   ├── styles/      # CSS styles
│   │   ├── utils/       # Utility functions
│   │   ├── views/       # Page components
│   │   ├── App.vue      # Root component
│   │   ├── main.js      # Entry point
│   │   └── style.css    # Global styles
│   ├── .env.development # Development environment variables
│   ├── index.html       # HTML template
│   ├── package.json     # npm dependencies
│   └── vite.config.js   # Vite configuration
├── src/                 # Java backend
│   ├── main/            # Main source code
│   │   ├── java/com/greenchain/backend/ # Java packages
│   │   │   ├── config/  # Configuration classes
│   │   │   ├── controller/ # REST controllers
│   │   │   ├── dto/     # Data transfer objects
│   │   │   ├── model/   # Entity classes
│   │   │   ├── repository/ # JPA repositories
│   │   │   ├── security/ # Security configuration
│   │   │   ├── service/ # Business logic
│   │   │   └── GreenChainBackendApplication.java # Main application class
│   │   └── resources/   # Resources
│   │       ├── application-*.properties # Configuration files
│   │       ├── data.sql  # Data initialization
│   │       ├── init.sql  # Database initialization
│   │       └── sample_data.sql # Sample data
│   └── test/            # Test classes
├── .gitignore           # Git ignore file
├── Dockerfile           # Dockerfile
├── docker-compose.yml   # Docker Compose configuration
└── pom.xml              # Maven pom file
```

## Getting Started

### Prerequisites
- Java 21 or higher
- Node.js 16 or higher
- npm 7 or higher
- MySQL 8.0 or higher

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd GreenChain
   ```

2. **Configure MySQL database**
   - Create a database named `greenchain`
   - Update the database credentials in `src/main/resources/application-dev.properties`

3. **Build and run the backend**
   ```bash
   # Build the project
   mvn clean package
   
   # Run the application
   mvn spring-boot:run
   ```

   The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to the frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Run the development server**
   ```bash
   npm run dev
   ```

   The frontend will start on `http://localhost:5173` or another available port

## Usage

### Accessing the Application
1. Open your browser and navigate to `http://localhost:5173`
2. Log in using the default credentials:
   - Username: `admin`
   - Password: `admin123`

### Navigating the Application
- **Home**: Overview of the platform with quick access to main features
- **Core Indicators**: View key sustainability metrics and charts
- **Carbon Emission Charts**: Analyze emissions by transportation mode and date
- **Supplier Chain Map**: Explore global suppliers and shipment routes on an interactive globe
- **Supplier Management**: Manage supplier information and certifications
- **Shipment Tracking**: Track and manage supply chain shipments
- **Generate Report**: Export sustainability data and charts to PDF

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/reset-password` - Reset password

### Dashboard
- `GET /api/dashboard/summary` - Get dashboard summary data

### Suppliers
- `GET /api/suppliers` - Get all suppliers
- `POST /api/suppliers` - Create a new supplier
- `PUT /api/suppliers/{id}` - Update a supplier
- `DELETE /api/suppliers/{id}` - Delete a supplier

### Shipments
- `GET /api/shipments` - Get all shipments
- `POST /api/shipments` - Create a new shipment
- `PUT /api/shipments/{id}` - Update a shipment
- `DELETE /api/shipments/{id}` - Delete a shipment

### Carbon
- `POST /api/carbon/calculate` - Calculate carbon emissions
- `GET /api/carbon/by-mode` - Get emissions by transportation mode
- `GET /api/carbon/by-date` - Get emissions by date

### Transport Modes
- `GET /api/transport-modes` - Get all transport modes

## Data Model

### Core Entities
- **User**: System user with authentication details
- **Supplier**: Supplier information including location and certification status
- **Shipment**: Shipment details including origin, destination, weight, and transportation mode
- **TransportMode**: Transportation mode with emission factors

### Relationships
- User has many Shipments
- Supplier has many Shipments (as origin or destination)
- Shipment has one TransportMode

## Contributing

1. Fork the repository
2. Create a new branch for your feature or bug fix
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
