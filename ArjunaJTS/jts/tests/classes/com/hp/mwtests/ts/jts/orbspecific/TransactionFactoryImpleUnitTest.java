/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jts.orbspecific;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.recovery.transactions.AssumedCompleteHeuristicTransaction;
import com.hp.mwtests.ts.jts.resources.TestBase;

/**
 * @author <a href="gytis@redhat.com">Gytis Trikleris</a>
 */
public final class TransactionFactoryImpleUnitTest extends TestBase {

    private TransactionFactoryImple transactionFactory;

    @BeforeClass
    public static void beforeClass() {
        arjPropertyManager.getObjectStoreEnvironmentBean().setObjectStoreType(ShadowNoFileLockStore.class.getName());
    }

    @Before
    public void before() {
        clearObjectStore();
        transactionFactory = new TransactionFactoryImple();
    }

    @After
    public void after() {
        clearObjectStore();
    }

    @Test
    public void testGetOSStatusNoTransaction() throws NoTransaction, SystemException {
        Assert.assertEquals(Status.StatusNoTransaction, transactionFactory.getOSStatus(new Uid()));
    }

    @Test
    public void testGetOSStatusWithArjunaTransactionImple() throws Exception {
        final Uid uid = new Uid();
        final OutputObjectState outputObjectState = new OutputObjectState();
        final ArjunaTransactionImple transaction = new ArjunaTransactionImple(uid, null);
        
        transaction.save_state(outputObjectState, ObjectType.ANDPERSISTENT);
        StoreManager.getRecoveryStore().write_committed(uid, ArjunaTransactionImple.typeName(), outputObjectState);
        
        Assert.assertEquals(Status.StatusCommitted, transactionFactory.getOSStatus(uid));
    }
    
    @Test
    public void testGetOSStatusWithAssumedCompleteHeuristicTransaction() throws Exception {
        final Uid uid = new Uid();
        final OutputObjectState outputObjectState = new OutputObjectState();
        final AssumedCompleteHeuristicTransaction transaction = new AssumedCompleteHeuristicTransaction(uid);

        transaction.save_state(outputObjectState, ObjectType.ANDPERSISTENT);
        StoreManager.getRecoveryStore().write_committed(uid, AssumedCompleteHeuristicTransaction.typeName(),
                outputObjectState);

        Assert.assertEquals(Status.StatusCommitted, transactionFactory.getOSStatus(uid));
    }

    private void clearObjectStore() {
        final String objectStorePath = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreDir();
        final File objectStoreDirectory = new File(objectStorePath);
        
        clearDirectory(objectStoreDirectory);
    }
    
    private void clearDirectory(final File directory) {
        final File[] files = directory.listFiles();
        
        if (files != null) {
            for (final File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                
                file.delete();
            }
        }
    }
    
}
