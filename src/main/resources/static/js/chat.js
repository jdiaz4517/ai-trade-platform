// Chat Application State
class ChatApp {
    constructor() {
        this.currentUser = null;
        this.currentSession = null;
        this.userType = 'CUSTOMER';
        this.sessionCounter = 1;
        this.messageHistory = [];
        
        this.initializeElements();
        this.attachEventListeners();
        this.loadCurrentEngine();
    }

    initializeElements() {
        // Login elements
        this.loginSection = document.getElementById('login-section');
        this.usernameInput = document.getElementById('username-input');
        this.userTypeSelect = document.getElementById('user-type-select');
        this.startChatBtn = document.getElementById('start-chat-btn');

        // Chat elements
        this.chatSection = document.getElementById('chat-section');
        this.userDisplay = document.getElementById('user-display');
        this.sessionDisplay = document.getElementById('session-display');
        this.chatMessages = document.getElementById('chat-messages');
        this.messageInput = document.getElementById('message-input');
        this.sendBtn = document.getElementById('send-btn');
        this.charCount = document.getElementById('char-count');
        this.newSessionBtn = document.getElementById('new-session-btn');
        this.logoutBtn = document.getElementById('logout-btn');
        this.loadingOverlay = document.getElementById('loading-overlay');
        this.currentEngineDisplay = document.getElementById('current-engine');
    }

    attachEventListeners() {
        // Login events
        this.startChatBtn.addEventListener('click', () => this.startChat());
        this.usernameInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.startChat();
        });
        this.usernameInput.addEventListener('input', () => this.validateLoginForm());

        // Chat events
        this.sendBtn.addEventListener('click', () => this.sendMessage());
        this.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        this.messageInput.addEventListener('input', () => this.updateCharCount());
        this.newSessionBtn.addEventListener('click', () => this.startNewSession());
        this.logoutBtn.addEventListener('click', () => this.logout());

        // Auto-resize textarea
        this.messageInput.addEventListener('input', () => this.autoResizeTextarea());
    }

    validateLoginForm() {
        const username = this.usernameInput.value.trim();
        this.startChatBtn.disabled = username.length < 2;
    }

    startChat() {
        const username = this.usernameInput.value.trim();
        if (username.length < 2) {
            this.showError('Username must be at least 2 characters long');
            return;
        }

        this.currentUser = username;
        this.userType = this.userTypeSelect.value;
        this.generateNewSession();
        
        this.userDisplay.textContent = `${username} (${this.userType.toLowerCase()})`;
        this.sessionDisplay.textContent = this.currentSession;
        
        // Switch to chat view
        this.loginSection.style.display = 'none';
        this.chatSection.style.display = 'flex';
        
        // Focus on message input
        this.messageInput.focus();
        
        console.log('Chat started:', { user: this.currentUser, session: this.currentSession, userType: this.userType });
    }

    generateNewSession() {
        const timestamp = new Date().toISOString().slice(0, 19).replace(/[-:]/g, '').replace('T', '_');
        this.currentSession = `${this.currentUser}_session_${this.sessionCounter}_${timestamp}`;
        this.sessionCounter++;
        this.messageHistory = [];
    }

    startNewSession() {
        this.generateNewSession();
        this.sessionDisplay.textContent = this.currentSession;
        
        // Clear chat messages except system message
        const systemMessage = this.chatMessages.querySelector('.system-message');
        this.chatMessages.innerHTML = '';
        if (systemMessage) {
            this.chatMessages.appendChild(systemMessage);
        }
        
        this.messageHistory = [];
        console.log('New session started:', this.currentSession);
    }

    logout() {
        this.currentUser = null;
        this.currentSession = null;
        this.userType = 'CUSTOMER';
        this.sessionCounter = 1;
        this.messageHistory = [];
        
        // Reset form
        this.usernameInput.value = '';
        this.userTypeSelect.value = 'CUSTOMER';
        this.messageInput.value = '';
        this.updateCharCount();
        
        // Clear chat messages
        const systemMessage = this.chatMessages.querySelector('.system-message');
        this.chatMessages.innerHTML = '';
        if (systemMessage) {
            this.chatMessages.appendChild(systemMessage);
        }
        
        // Switch to login view
        this.chatSection.style.display = 'none';
        this.loginSection.style.display = 'flex';
        
        console.log('Logged out');
    }

    updateCharCount() {
        const length = this.messageInput.value.length;
        this.charCount.textContent = length;
        
        // Enable/disable send button
        this.sendBtn.disabled = length === 0 || length > 500;
        
        // Color coding for character count
        if (length > 450) {
            this.charCount.style.color = '#dc3545';
        } else if (length > 400) {
            this.charCount.style.color = '#fd7e14';
        } else {
            this.charCount.style.color = '#6c757d';
        }
    }

    autoResizeTextarea() {
        this.messageInput.style.height = 'auto';
        this.messageInput.style.height = Math.min(this.messageInput.scrollHeight, 120) + 'px';
    }

    async sendMessage() {
        const message = this.messageInput.value.trim();
        if (!message || message.length > 500) return;

        // Add user message to chat
        this.addMessageToChat('user', message);
        
        // Clear input
        this.messageInput.value = '';
        this.updateCharCount();
        this.autoResizeTextarea();

        // Show loading
        this.showLoading(true);

        try {
            // Build request in the format you specified
            const requestData = {
                message: message,
                sessionId: this.currentSession,
                userId: this.currentUser,
                userType: this.userType
            };

            console.log('Sending request:', requestData);

            // Call your API
            const response = await fetch('/api/chat/ui', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(requestData)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log('Received response:', data);

            // Add AI response to chat
            this.addMessageToChat('assistant', data.message, data);

        } catch (error) {
            console.error('Error sending message:', error);
            this.addMessageToChat('assistant', 'Sorry, I encountered an error while processing your request. Please try again.', null, true);
        } finally {
            this.showLoading(false);
        }
    }

    addMessageToChat(sender, content, responseData = null, isError = false) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;

        const bubbleDiv = document.createElement('div');
        bubbleDiv.className = 'message-bubble';
        bubbleDiv.textContent = content;

        if (isError) {
            bubbleDiv.style.background = '#f8d7da';
            bubbleDiv.style.color = '#721c24';
            bubbleDiv.style.borderColor = '#f5c6cb';
        }

        const infoDiv = document.createElement('div');
        infoDiv.className = 'message-info';
        
        const timestamp = new Date().toLocaleTimeString();
        if (sender === 'user') {
            infoDiv.textContent = `You • ${timestamp}`;
        } else {
            let engineInfo = '';
            if (responseData && responseData.extractedInfo) {
                const extracted = responseData.extractedInfo;
                const extractedKeys = Object.keys(extracted).filter(key => 
                    extracted[key] !== null && 
                    extracted[key] !== undefined && 
                    key !== 'timestamp' && 
                    key !== 'messageLength'
                );
                if (extractedKeys.length > 0) {
                    engineInfo = ` • Extracted: ${extractedKeys.join(', ')}`;
                }
            }
            infoDiv.textContent = `AI Assistant • ${timestamp}${engineInfo}`;
        }

        messageDiv.appendChild(bubbleDiv);
        messageDiv.appendChild(infoDiv);
        this.chatMessages.appendChild(messageDiv);

        // Scroll to bottom
        this.chatMessages.scrollTop = this.chatMessages.scrollHeight;

        // Store in history
        this.messageHistory.push({
            sender,
            content,
            timestamp: new Date().toISOString(),
            responseData
        });
    }

    showLoading(show) {
        this.loadingOverlay.style.display = show ? 'flex' : 'none';
        this.sendBtn.disabled = show || this.messageInput.value.trim().length === 0;
    }

    showError(message) {
        // Simple alert for now - could be enhanced with a toast notification
        alert(message);
    }

    async loadCurrentEngine() {
        try {
            const response = await fetch('/api/chat/engine-status');
            if (response.ok) {
                const data = await response.json();
                this.currentEngineDisplay.textContent = `Engine: ${data.activeEngine.toUpperCase()}`;
            } else {
                this.currentEngineDisplay.textContent = 'Engine: Unknown';
            }
        } catch (error) {
            this.currentEngineDisplay.textContent = 'Engine: Offline';
            console.error('Could not load engine status:', error);
        }
    }
}

// Initialize the chat application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
    console.log('AI Trade Platform Chat Interface loaded');
});

// Handle page refresh/close
window.addEventListener('beforeunload', (e) => {
    if (window.chatApp && window.chatApp.currentUser) {
        e.preventDefault();
        e.returnValue = 'You have an active chat session. Are you sure you want to leave?';
    }
});