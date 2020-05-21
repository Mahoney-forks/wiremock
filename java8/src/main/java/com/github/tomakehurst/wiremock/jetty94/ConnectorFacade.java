package com.github.tomakehurst.wiremock.jetty94;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.Container;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

class ConnectorFacade implements Connector {

    private final Connector delegate;

    ConnectorFacade(Connector delegate) {
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

    @Override
    public boolean addBean(Object o) {
        return delegate.addBean(o);
    }

    @Override
    public boolean addBean(Object o, boolean managed) {
        return delegate.addBean(o, managed);
    }

    @Override
    public Collection<Object> getBeans() {
        return delegate.getBeans();
    }

    @Override
    public <T> Collection<T> getBeans(Class<T> clazz) {
        return delegate.getBeans(clazz);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return delegate.getBean(clazz);
    }

    @Override
    public boolean removeBean(Object o) {
        return delegate.removeBean(o);
    }

    @Override
    public void addEventListener(Container.Listener listener) {
        delegate.addEventListener(listener);
    }

    @Override
    public void removeEventListener(Container.Listener listener) {
        delegate.removeEventListener(listener);
    }

    @Override
    public void unmanage(Object bean) {
        delegate.unmanage(bean);
    }

    @Override
    public void manage(Object bean) {
        delegate.manage(bean);
    }

    @Override
    public boolean isManaged(Object bean) {
        return delegate.isManaged(bean);
    }

    @Override
    public <T> Collection<T> getContainedBeans(Class<T> clazz) {
        return delegate.getContainedBeans(clazz);
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }
}
