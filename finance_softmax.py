from sklearn.cross_validation import train_test_split
from tensorflow.python.framework import ops

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import tensorflow as tf
from tf_utils import random_mini_batches
import pickle
import time


def loaddata():
    rawdata = pd.read_csv("dataset/rawdata_softmax.csv")
    print(rawdata.info)
    
    sector_dictionary = {'Basic Materials':1,
'Capital Goods':2,
'Conglomerates':3,
'Consumer Cyclical':4,
'Consumer/Non-Cyclical':5,
'Energy':6,
'Financial':7,
'Healthcare':8,
'Services':9,
'Technology':10,
'Transportation':11,
'Utilities':12 }

    #traindata = rawdata[['pctchange','slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector', 'diapctchange', 'iwmpctchange', 'oneqpctchange']]
    traindata = rawdata[['pctchange','slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector']]
    traindata['sector'] = traindata['sector'].apply(lambda x: sector_dictionary[x])
    labeldata = rawdata[['dn3', 'dn2', 'dn1', 'dn0', 'up0', 'up1', 'up2', 'up3']]
        
    fill_value = pd.DataFrame({col: traindata.mean(axis=1) for col in traindata.columns})
    traindata.fillna(fill_value, inplace=True)
    print(traindata.describe)
    print(labeldata.describe)
    # print(traindata.info) # your train set features
    # print(labeldata.info) # your train set labels
    
    # adjust prediction to next number of days
    days = 0
    traindata = traindata[0:traindata.shape[0] - 1 - days]
    labeldata = labeldata[days:labeldata.shape[0] - 1]
    
    trainX , testX , trainy , testy = train_test_split(traindata , labeldata , train_size=.9)
    # print(trainy.shape)
    # print(testy.shape)
    return trainX , trainy, testX, testy
    

# Loading the dataset
X_train_orig, Y_train_orig, X_test_orig, Y_test_orig = loaddata()
X_train = np.asmatrix(X_train_orig.T)
Y_train = np.asmatrix(Y_train_orig.T)
X_test = np.asmatrix(X_test_orig.T)
Y_test = np.asmatrix(Y_test_orig.T)

print ("number of training examples = " + str(X_train.shape[1]))
print ("number of test examples = " + str(X_test.shape[1]))
print ("X_train shape: " + str(X_train.shape))
print ("Y_train shape: " + str(Y_train.shape))
print ("X_test shape: " + str(X_test.shape))
print ("Y_test shape: " + str(Y_test.shape))
print("="*20)
print(X_train.shape)
print(Y_train.shape)
print(X_test.shape)
print(Y_test.shape)
# print(classes.shape)
print("="*20)

# quit()


def create_placeholders(n_x, n_y):
    X = tf.placeholder(tf.float32, shape=[n_x, None], name='X')
    Y = tf.placeholder(tf.float32, shape=[n_y, None], name='Y')
    return X, Y


def initialize_parameters():
    W1 = tf.get_variable('W1', [25, X_train.shape[0]], initializer=tf.contrib.layers.xavier_initializer(seed=1))
    b1 = tf.get_variable('b1', [25, 1], initializer=tf.zeros_initializer())
    W2 = tf.get_variable('W2', [12, 25], initializer=tf.contrib.layers.xavier_initializer(seed=1))
    b2 = tf.get_variable('b2', [12, 1], initializer=tf.zeros_initializer())
    W3 = tf.get_variable('W3', [Y_train.shape[0], 12], initializer=tf.contrib.layers.xavier_initializer(seed=1))
    b3 = tf.get_variable('b3', [Y_train.shape[0], 1], initializer=tf.zeros_initializer())
    parameters = {"W1": W1,
                  "b1": b1,
                  "W2": W2,
                  "b2": b2,
                  "W3": W3,
                  "b3": b3}
    return parameters


def forward_propagation(X, parameters):
    """
    Implements the forward propagation for the model: LINEAR -> RELU -> LINEAR -> RELU -> LINEAR -> SOFTMAX
    
    Arguments:
    X -- input dataset placeholder, of shape (input size, number of examples)
    parameters -- python dictionary containing your parameters "W1", "b1", "W2", "b2", "W3", "b3"
                  the shapes are given in initialize_parameters

    Returns:
    Z3 -- the output of the last LINEAR unit
    """
    
    # Retrieve the parameters from the dictionary "parameters" 
    W1 = parameters['W1']
    b1 = parameters['b1']
    W2 = parameters['W2']
    b2 = parameters['b2']
    W3 = parameters['W3']
    b3 = parameters['b3']

    Z1 = tf.add(tf.matmul(W1, X), b1)  # Z1 = np.dot(W1, X) + b1
    A1 = tf.nn.relu(Z1)  # A1 = relu(Z1)
    Z2 = tf.add(tf.matmul(W2, A1), b2)  # Z2 = np.dot(W2, a1) + b2
    A2 = tf.nn.relu(Z2)  # A2 = relu(Z2)
    Z3 = tf.add(tf.matmul(W3, A2), b3)  # Z3 = np.dot(W3,Z2) + b3
    return Z3

def initialize_parameters_deep(layer_dims):
    parameters = {}
    L = len(layer_dims)  # number of layers in the network

    for l in range(1, L):
        parameters['W' + str(l)] = tf.get_variable('W' + str(l), [layer_dims[l], layer_dims[l - 1]], initializer=tf.contrib.layers.xavier_initializer(seed=1))
        parameters['b' + str(l)] = tf.get_variable('b' + str(l), [layer_dims[l], 1], initializer=tf.zeros_initializer())
        
        assert(parameters['W' + str(l)].shape == (layer_dims[l], layer_dims[l - 1]))
        assert(parameters['b' + str(l)].shape == (layer_dims[l], 1))
        
    return parameters


def forward_propagation_deep(X, parameters, layer_dims):
    L = len(layer_dims)
    A = X
    
    for l in range(1, L):
        Z = tf.add(tf.matmul(parameters['W' + str(l)], A), parameters['b' + str(l)])
        A = tf.nn.relu(Z) 
    
    return Z

def compute_cost(Z, Y):
    """
    Arguments:
    Z3 -- output of forward propagation (output of the last LINEAR unit)
    Y -- "true" labels vector placeholder, same shape as Z3
    
    Returns:
    cost - Tensor of the cost function
    """
    
    # to fit the tensorflow requirement for tf.nn.softmax_cross_entropy_with_logits(...,...)
    logits = tf.transpose(Z)
    labels = tf.transpose(Y)
    
    cost = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(logits=logits, labels=labels))
    return cost


def model(X_train, Y_train, X_test, Y_test, layer_dims, learning_rate=0.0001,
          num_epochs=3000, minibatch_size=32, print_cost=True):
    """
    Implements a three-layer tensorflow neural network: LINEAR->RELU->LINEAR->RELU->LINEAR->SOFTMAX.
    
    Arguments:
    X_train -- training set, of shape
    Y_train -- test set, of shape
    X_test -- training set, of shape
    Y_test -- test set, of shape
    learning_rate -- learning rate of the optimization
    num_epochs -- number of epochs of the optimization loop
    minibatch_size -- size of a minibatch
    print_cost -- True to print the cost every 100 epochs
    
    Returns:
    parameters -- parameters learnt by the model. They can then be used to predict.
    """
    print("layer_dims=" + str(layer_dims))
    print("learning_rate=" + str(learning_rate))
    
    ops.reset_default_graph()  # to be able to rerun the model without overwriting tf variables
    tf.set_random_seed(1)  # to keep consistent results
    seed = 3  # to keep consistent results
    (n_x, m) = X_train.shape  # (n_x: input size, m : number of examples in the train set)
    n_y = Y_train.shape[0]  # n_y : output size
    costs = []  # To keep track of the cost
    
    # Create Placeholders of shape (n_x, n_y)
    X, Y = create_placeholders(n_x, n_y)

    # Initialize parameters
    parameters = initialize_parameters_deep(layer_dims)
    
    # Forward propagation: Build the forward propagation in the tensorflow graph
    Z = forward_propagation_deep(X, parameters, layer_dims)
    
    # Cost function: Add cost function to tensorflow graph
    cost = compute_cost(Z, Y)
    
    # Backpropagation: Define the tensorflow optimizer. Use an AdamOptimizer.
    optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)
    
    # Initialize all the variables
    init = tf.global_variables_initializer()

    # Start the session to compute the tensorflow graph
    with tf.Session() as sess:
        
        # Run the initialization
        sess.run(init)
        
        # Do the training loop
        for epoch in range(num_epochs):

            epoch_cost = 0.  # Defines a cost related to an epoch
            num_minibatches = int(m / minibatch_size)  # number of minibatches of size minibatch_size in the train set
            seed = seed + 1
            minibatches = random_mini_batches(X_train, Y_train, minibatch_size, seed)

            for minibatch in minibatches:

                # Select a minibatch
                (minibatch_X, minibatch_Y) = minibatch
                
                # IMPORTANT: The line that runs the graph on a minibatch.
                # Run the session to execute the "optimizer" and the "cost", the feedict should contain a minibatch for (X,Y).
                _ , minibatch_cost = sess.run([optimizer, cost], feed_dict={X: minibatch_X, Y: minibatch_Y})
                
                epoch_cost += minibatch_cost / num_minibatches

            # Print the cost every epoch
            if print_cost == True and epoch % 100 == 0:
                print ("Cost after epoch %i: %f" % (epoch, epoch_cost))
            if print_cost == True and epoch % 5 == 0:
                costs.append(epoch_cost)
                
        # plot the cost
        plt.plot(np.squeeze(costs))
        
        plt.ylabel('cost')
        plt.xlabel('iterations (per tens)')
        plt.title("Learning rate =" + str(learning_rate))
        plt.show()

        # lets save the parameters in a variable
        parameters = sess.run(parameters)
        print ("Parameters have been trained!")

        # Calculate the correct predictions
        correct_prediction = tf.equal(tf.argmax(Z), tf.argmax(Y))

        # Calculate accuracy on the test set
        accuracy = tf.reduce_mean(tf.cast(correct_prediction, "float"))

        print ("Train Accuracy:", accuracy.eval({X: X_train, Y: Y_train}))
        print ("Test Accuracy:", accuracy.eval({X: X_test, Y: Y_test}))
        
        return parameters


# starts here
sess = tf.Session()

layer_dims = [X_train.shape[0], 25, 12, 8]

parameters = model(X_train, Y_train, X_test, Y_test, layer_dims, num_epochs=3000)

print(parameters)

timestr = time.strftime("%Y%m%d-%H%M%S")
print("Saved as " + "tf_softmax_nn-" + timestr + ".pickle")
pickle_out = open("tf_softmax_nn-" + timestr + ".pickle", "wb")
pickle.dump(parameters, pickle_out)
