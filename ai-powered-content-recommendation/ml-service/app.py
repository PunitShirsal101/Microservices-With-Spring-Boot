from flask import Flask, request, jsonify
import numpy as np
import tensorflow as tf
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)

# Placeholder user-item matrix for recommendations
rng = np.random.default_rng(seed=42)
user_item_matrix = rng.random((10, 20))  # 10 users, 20 items

# Simple TensorFlow model for demonstration
model = tf.keras.Sequential([
    tf.keras.layers.Dense(64, activation='relu', input_shape=(20,)),
    tf.keras.layers.Dense(32, activation='relu'),
    tf.keras.layers.Dense(20, activation='sigmoid')
])
model.compile(optimizer='adam', loss='mse')

@app.route('/', methods=['GET'])
def home():
    return jsonify({"message": "AI-Powered Content Recommendation ML Service is running", "status": "healthy"})

@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.get_json()
    user_id = data.get('user_id')
    if not user_id:
        return jsonify({"error": "user_id required"}), 400
    
    # Use TensorFlow model for prediction (placeholder)
    user_index = int(user_id) % 10
    user_vector = user_item_matrix[user_index]
    prediction = model.predict(np.array([user_vector]))[0]
    recommended_indices = np.argsort(prediction)[-3:][::-1]
    recommendations = [f"content{i}" for i in recommended_indices]
    
    return jsonify({"user_id": user_id, "recommendations": recommendations})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
