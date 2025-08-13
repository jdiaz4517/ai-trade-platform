# AI Trade Platform

An AI-powered platform that connects customers with skilled tradespeople through conversational interfaces.

## Features

- **Conversational AI**: Natural language interaction for both customers and tradespeople
- **Smart Matching**: AI extracts requirements and matches customers with suitable trades
- **Dual User Types**: Separate flows for customers seeking services and tradespeople offering services  
- **Session Management**: Maintains conversation context across interactions
- **Real-time Chat**: RESTful API for chat interactions

## Tech Stack

- **Spring Boot 3.2.1** with Java 21
- **Spring AI 0.8.1** for AI integration
- **Ollama** with Mistral model for local AI inference
- **H2 Database** (development) 
- **Maven** for build management

## Prerequisites

1. **Java 21** installed
2. **Ollama** running locally with Mistral model
3. **Maven 3.6+**

### Setting up Ollama

```bash
# Install Ollama (if not already installed)
curl -fsSL https://ollama.ai/install.sh | sh

# Pull Mistral model
ollama pull mistral

# Verify Ollama is running
curl http://localhost:11434/api/tags
```

## Quick Start

1. **Clone and navigate to project**:
   ```bash
   cd ai-trade-platform
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Test the API**:
   ```bash
   # Health check
   curl http://localhost:8081/api/chat/health
   
   # Customer conversation
   curl -X POST http://localhost:8081/api/chat/customer \
     -H "Content-Type: application/json" \
     -d '{
       "message": "I need a plumber in London for a leaky faucet, budget around £150",
       "sessionId": "test-session-1"
     }'
   
   # Tradesperson conversation  
   curl -X POST http://localhost:8081/api/chat/tradesperson \
     -H "Content-Type: application/json" \
     -d '{
       "message": "I am a qualified electrician in Manchester, available weekdays",
       "sessionId": "test-session-2"
     }'
   ```

## API Endpoints

- `GET /api/chat/health` - Health check
- `POST /api/chat/message` - General chat endpoint
- `POST /api/chat/customer` - Customer-specific chat  
- `POST /api/chat/tradesperson` - Tradesperson-specific chat
- `DELETE /api/chat/session/{sessionId}` - Clear conversation history

## Example Conversations

### Customer Flow
```
Customer: "I need a plumber in London for emergency pipe repair"
AI: "I understand you need emergency plumbing in London for pipe repair. What's your budget range and when do you need this completed?"

Customer: "Budget is £200-300, needed today if possible"  
AI: "Perfect! I have your requirements: Emergency plumbing, London, pipe repair, £200-300 budget, needed today. Let me find qualified plumbers in your area..."
```

### Tradesperson Flow  
```
Tradesperson: "I'm a certified electrician in Birmingham, 10 years experience"
AI: "Great! I've noted you're a certified electrician in Birmingham with 10 years experience. What types of electrical work do you specialize in, and what's your availability?"

Tradesperson: "I do residential rewiring, fault finding, and installations. Available Mon-Fri 8am-6pm"
AI: "Excellent! I have some customer requests for electrical work in Birmingham that match your skills..."
```

## Configuration

Key configuration in `application.yml`:

- **Ollama settings**: Base URL, model selection, temperature
- **Database**: H2 in-memory for development  
- **Logging**: Debug level for AI components
- **System prompts**: AI behavior customization

## Development Notes

- Conversation history stored in-memory (implement Redis/Database for production)
- Basic keyword extraction (enhance with NLP libraries)
- H2 console available at: http://localhost:8081/h2-console
- Information extraction logic in `ChatService.extractInformation()`

## Next Steps

1. **Database Integration**: Replace in-memory storage with persistent database
2. **Advanced NLP**: Implement proper named entity recognition
3. **Matching Algorithm**: Build sophisticated tradesperson-customer matching
4. **Authentication**: Add user authentication and authorization  
5. **WebSocket Support**: Real-time bidirectional communication
6. **Integration**: Connect with your existing place-intel-service for location data