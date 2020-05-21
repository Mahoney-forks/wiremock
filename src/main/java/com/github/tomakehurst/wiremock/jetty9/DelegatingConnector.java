package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

class DelegatingConnector implements Connector {

    private final Connector delegate;

    DelegatingConnector(Connector delegate) {
        this.delegate = delegate;
    }

    @Override
    public Server getServer() {
        return delegate.getServer();
    }

    @Override
    public Executor getExecutor() {
        return delegate.getExecutor();
    }

    @Override
    public Scheduler getScheduler() {
        return delegate.getScheduler();
    }

    @Override
    public ByteBufferPool getByteBufferPool() {
        return delegate.getByteBufferPool();
    }

    @Override
    public ConnectionFactory getConnectionFactory(String nextProtocol) {
        return delegate.getConnectionFactory(nextProtocol);
    }

    @Override
    public <T> T getConnectionFactory(Class<T> factoryType) {
        return delegate.getConnectionFactory(factoryType);
    }

    @Override
    public ConnectionFactory getDefaultConnectionFactory() {
        return delegate.getDefaultConnectionFactory();
    }

    @Override
    public Collection<ConnectionFactory> getConnectionFactories() {
        return delegate.getConnectionFactories();
    }

    @Override
    public List<String> getProtocols() {
        return delegate.getProtocols();
    }

    @Override
    @ManagedAttribute("maximum time a connection can be idle before being closed (in ms)")
    public long getIdleTimeout() {
        return delegate.getIdleTimeout();
    }

    @Override
    public Object getTransport() {
        return delegate.getTransport();
    }

    @Override
    public Collection<EndPoint> getConnectedEndPoints() {
        return delegate.getConnectedEndPoints();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    @ManagedOperation(value = "Starts the instance", impact = "ACTION")
    public void start() throws Exception {
        delegate.start();
    }

    @Override
    @ManagedOperation(value = "Stops the instance", impact = "ACTION")
    public void stop() throws Exception {
        delegate.stop();
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public boolean isStarting() {
        return delegate.isStarting();
    }

    @Override
    public boolean isStopping() {
        return delegate.isStopping();
    }

    @Override
    public boolean isStopped() {
        return delegate.isStopped();
    }

    @Override
    public boolean isFailed() {
        return delegate.isFailed();
    }

    @Override
    public void addLifeCycleListener(LifeCycle.Listener listener) {
        delegate.addLifeCycleListener(listener);
    }

    @Override
    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        delegate.removeLifeCycleListener(listener);
    }

    @Override
    public Future<Void> shutdown() {
        return delegate.shutdown();
    }
}
