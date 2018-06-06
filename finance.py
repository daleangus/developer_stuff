'''
Created on Apr 30, 2018

@author: Dale Angus (daleangus@hotmail.com)
'''
import math
from sklearn.cross_validation import train_test_split
from dnn_app_utils_v3 import *
import matplotlib.pyplot as plt
import pandas as pd

def loaddata():
    rawdata = pd.read_csv("dataset/rawdata.csv")
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

    traindata = rawdata[['pctchange', 'slope10', 'slope21', 'awesomeoscillator', 'momentum34', 'tangentslope', 'open', 'high', 'low', 'weighting', 'vixclose', 'vixpctchange', 'sector', 'diapctchange', 'iwmpctchange', 'oneqpctchange']]
    # traindata['sector'] = traindata_tmp['sector'].apply(lambda x: sector_dictionary[x])
    labeldata = rawdata[['spyupdown']]
        
    fill_value = pd.DataFrame({col: traindata.mean(axis=1) for col in traindata.columns})
    traindata.fillna(fill_value, inplace=True)
    print(traindata.describe)
    print(labeldata.describe)
    # print(traindata.info) # your train set features
    # print(labeldata.info) # your train set labels
    
    trainX , testX , trainy , testy = train_test_split(traindata , labeldata.values , train_size=.9)
    return trainX , testX , trainy.reshape(1, trainy.shape[0]) , testy.reshape(1, testy.shape[0])
    

def L_layer_model(X, Y, layers_dims, learning_rate=0.0075, num_iterations=3000, print_cost=False):  # lr was 0.009
    """
    Implements a L-layer neural network: [LINEAR->RELU]*(L-1)->LINEAR->SIGMOID.
    
    Arguments:
    X -- data, numpy array of shape (number of examples, num_px * num_px * 3)
    Y -- true "label" vector (containing 0 if cat, 1 if non-cat), of shape (1, number of examples)
    layers_dims -- list containing the input size and each layer size, of length (number of layers + 1).
    learning_rate -- learning rate of the gradient descent update rule
    num_iterations -- number of iterations of the optimization loop
    print_cost -- if True, it prints the cost every 100 steps
    
    Returns:
    parameters -- parameters learnt by the model. They can then be used to predict.
    """

    np.random.seed(1)
    costs = []  # keep track of cost
    print("Doing layer_dims=" + str(layers_dims))
    # Parameters initialization. (≈ 1 line of code)
    parameters = initialize_parameters_deep(layers_dims)
    # Loop (gradient descent)
    for i in range(0, num_iterations):
        # Forward propagation: [LINEAR -> RELU]*(L-1) -> LINEAR -> SIGMOID.
        AL, caches = L_model_forward(X, parameters)
        # Compute cost.
        cost = compute_cost(AL, Y)
        # Backward propagation.
        grads = L_model_backward(AL, Y, caches)
        # Update parameters.
        parameters = update_parameters(parameters, grads, learning_rate)
                
        # Print the cost every 100 training example
        if print_cost and i % 100 == 0:
            print ("Cost after iteration %i: %f" % (i, cost))
        if print_cost and i % 100 == 0:
            costs.append(cost)
        if i % 100 == 0 and i >= 100:
            print(abs(costs[i // 100] - costs[i // 100 - 1]))
            if abs(costs[i // 100] - costs[i // 100 - 1]) < .000007:
                break
        if math.isnan(cost):
            break   
            
    # plot the cost
    plt.plot(np.squeeze(costs))
    plt.ylabel('cost')
    plt.xlabel('iterations (per tens)')
    plt.title("Learning rate =" + str(learning_rate))
    plt.show()
    
    return parameters


# Starts here    
train_x_orig, test_x_orig, train_y, test_y = loaddata()

train_x = train_x_orig.T
test_x = test_x_orig.T

print(train_x)
print(train_x.shape)
print(test_x.shape)
print(train_y.shape)
print(test_y.shape)

layers_dims = [train_x.shape[0], 10, 8, 6, 4, 2, 1]

learningrate = .0075
# learningrate = np.power(10, -4 * np.random.rand())

print("learning rate: " + str(learningrate))
parameters = L_layer_model(train_x, train_y, layers_dims , num_iterations=100000, print_cost=True, learning_rate=learningrate)
pred_train = predict(train_x, train_y, parameters)
pred_test = predict(test_x, test_y, parameters)

# print(parameters)
