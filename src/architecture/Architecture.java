package architecture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;

import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;

public class Architecture {

    private boolean simulation; //this boolean indicates if the execution is done in simulation mode.
    //simulation mode shows the components' status after each instruction

    private boolean halt;
    private Bus extbus1;
    private Bus intbus1;
    private Bus intbus2;
    private Memory memory;
    private Memory statusMemory;
    private int memorySize;
    private Register PC;
    private Register IR;
    private Register REG0, REG1, REG2, REG3;
    private Register Flags;
    private Register StkTOP, StkBOT;
    private Ula ula;
    private Demux demux; //only for multiple register purposes
    
    private boolean MemoryKey;

    private ArrayList<String> commandsList;
    private ArrayList<Register> registersList;

    /**
     * Instanciates all components in this architecture
     */
    private void componentsInstances() {
        
        extbus1 = new Bus();
        intbus1 = new Bus();
        intbus2 = new Bus();
        PC = new Register("PC", extbus1, intbus2);
        IR = new Register("IR", extbus1, intbus2);
        REG0 = new Register("REG0", intbus1, intbus2);
        REG1 = new Register("REG1", intbus1, intbus2);
        REG2 = new Register("REG2", intbus1, intbus2);
        REG3 = new Register("REG3", intbus1, intbus2);
        StkTOP = new Register("StkTOP", extbus1, null);
        StkBOT = new Register("StkBOT", extbus1, null);
        Flags = new Register(2, intbus2);
        fillRegistersList();
        ula = new Ula(intbus1, intbus2);
        statusMemory = new Memory(2, extbus1);
        memorySize = 128;
        memory = new Memory(memorySize, extbus1);
        demux = new Demux();

        fillCommandsList();
    }

    
    private void fillRegistersList() {
        registersList = new ArrayList<Register>();
        // Primeiro a serem inseridos, REGS
        registersList.add(REG0);
        registersList.add(REG1);
        registersList.add(REG2);
        registersList.add(REG3);
        registersList.add(PC);
        registersList.add(IR);
        registersList.add(Flags);
        registersList.add(StkTOP);
        registersList.add(StkBOT);
    }

    /**
     * Constructor that instanciates all components according the architecture
     * diagram
     */
    public Architecture() {
        componentsInstances();

        //by default, the execution method is never simulation mode
        simulation = false;
    }

    public Architecture(boolean sim, boolean key) {
        componentsInstances();

      // Aqui basicamente ativa a simulação
        simulation = sim;
        MemoryKey = key;
    }

    //getters
    protected Bus getExtbus1() {
        return extbus1;
    }

    protected Bus getIntbus1() {
        return intbus1;
    }

    protected Bus getIntbus2() {
        return intbus2;
    }

    protected Memory getMemory() {
        return memory;
    }

    protected Register getPC() {
        return PC;
    }

    protected Register getIR() {
        return IR;
    }

    protected Register getRPG() {
        return REG0;
    }

    protected Register getFlags() {
        return Flags;
    }

    protected Ula getUla() {
        return ula;
    }

    public ArrayList<String> getCommandsList() {
        return commandsList;
    }

    /**
     * This method fills the commands list arraylist with all commands used in
     */
    protected void fillCommandsList() {
        commandsList = new ArrayList<String>();
        commandsList.add("addMemReg"); //0
        commandsList.add("addRegMem"); //1
        commandsList.add("addRegARegB"); //2
        commandsList.add("addImmReg");   //3
        commandsList.add("subRegARegB");   //4
        commandsList.add("subMemReg");   //5
        commandsList.add("subRegMem");   //6
        commandsList.add("subImmReg");   //7
        commandsList.add("jmp");   //8
        commandsList.add("jz");    //9
        commandsList.add("jn");    //10
        commandsList.add("inc");   //11
        commandsList.add("moveMemReg"); //12
        commandsList.add("moveRegMem"); //13
        commandsList.add("moveRegReg"); //14
        commandsList.add("moveImmReg"); //15
        commandsList.add("jeq");       // 16
        commandsList.add("jneq");      // 17
        commandsList.add("jgt");       // 18
        commandsList.add("jlw");       // 19
        commandsList.add("call");      // 20
        commandsList.add("ret");       // 21
        commandsList.add("startStk"); //22

    }

    /**
     * This method is used after some ULA operations, setting the flags bits
     * according the result.
     */
    private void setStatusFlags(int result) {
        Flags.setBit(0, 0);
        Flags.setBit(1, 0);
        if (result == 0) { //bit 0 in flags must be 1 in this case
            Flags.setBit(0, 1);
        }
        if (result < 0) { //bit 1 in flags must be 1 in this case
            Flags.setBit(1, 1);
        }
    }
//all the microprograms must be impemented here
//the instructions table is
    /*
 add %<regA> %<regB>        RegB ← RegA + RegB                    
 add <mem> %<regA>          RegA ← memória[mem] + RegA           
 add %<regA> <mem>          Memória[mem] ← RegA + memória[mem]    
 add imm %<regA>            RegA ← imm + RegA                      
 sub <regA> <regB>          RegB ← RegA - RegB                    
 sub <mem> %<regA>          RegA ← memória[mem] - RegA             
 sub %<regA> <mem>          memória[mem] ← RegA - memória[mem]    
 sub imm %<regA>            RegA ← imm - RegA                      
 move <mem> %<regA>         RegA ← memória[mem]                    
 move %<regA> <mem>         memória[mem] ← RegA                    
 move %<regA> %<regB>       RegB ← RegA                            
 move imm %<regA>           RegA ← immediate                       
 inc %<regA>                RegA++                                 
 jmp <mem>                  PC ← mem                               
 jn <mem>                   Se última operação < 0, então PC ← mem 
 jz <mem>                   Se última operação = 0, então PC ← mem 
 jeq %<regA> %<regB> <mem>   Se RegA == RegB, então PC ← mem        
 jneq %<regA> %<regB> <mem>  Se RegA != RegB, então PC ← mem        
 jgt %<regA> %<regB> <mem>   Se RegA > RegB, então PC ← mem        
 jlw %<regA> %<regB> <mem>   Se RegA < RegB, então PC ← mem         
 call <mem>                  Empilha PC e desvia para mem           
 ret                         PC ← pop()    
 
 Imul  -_- press f
*/

    
    
    /*ADD Mem Reg
	
	*  1. pc -> intbus2         
	*  2. ula <- intbus2 
	*  3. ula.inc		  	 
	*  4. ula -> intbus2          
	*  5. pc <- intbus2           
	*  6. pc -> extbus1            
	*  7. memory.read() 		 
	*  8. memory.read() 		 
	*  9. IR <- extbus1             
	* 10. pc -> intbus2             
	* 11. ula <- intbus2    
	* 12. ula.inc  
	* 13. ula -> intbus2   
	* 14. pc <- intbus2        
	* 15. pc -> extbus1          
	* 16. memory.read()    
	* 17. demux <- extbus1         
	* 18. registers -> intbus2     
	* 19. ula <- intbus2           
	* 20. IR <- intbus2           
	* 21. ula <- intbus2        
	* 22. ula.add                  
	* 23. ula -> intbus2         
	* 24. flags <- intbus2         
	* 25. registers -> intbus2  
	* 26. pc -> intbus2             
	* 27. ula <- intbus2           
	* 28. ula.inc                
	* 29. ula -> intbus2          
	* 30. pc <- intbus2   
	* end
	*/
    public void addMemReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        memory.read();
        IR.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        IR.internalRead();
        ula.internalStore(0);
        ula.add();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	*  ADD RegA RegB  
	*  1. pc -> intbus2
	*  2. ula <- intbus2 
	*  3. ula.inc	
	*  4. ula -> intbus2
	*  5. pc <- intbus2 
	*  6. pc -> extbus1    
	*  7. memory.read() 
	*  8. pc -> intbus2 
	*  9. ula <- intbus2   
	* 10. ula.inc   
	* 11. ula -> intbus2 
	* 12. pc <- intbus2   
	* 13. demux <- extbus1   
	* 14. registers -> intbus2 
	* 15. ula <- intbus2
	* 16. pc -> extbus1 
	* 17. memory.read()   
	* 18. demux <- extbus1    
	* 19. registers -> intbus2   
	* 20. ula <- intbus2         
	* 21. ula.add           
	* 22. ula -> intbus2 
	* 23. flags <- intbus2     
	* 24. registers -> intbus2 
	* 25. pc -> intbus2  
	* 26. ula <- intbus2   
	* 27. ula.inc  
	* 28. ula -> intbus2         
	* 29. pc <- intbus2          
	* end
	*/
    public void addRegARegB() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.add();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	* 
	*  1. pc -> intbus2 
	*  2. ula <- intbus2 
	*  3. ula.inc		
	*  4. ula -> intbus2
	*  5. pc <- intbus2  
	*  6. pc -> extbus1 
	*  7. memory.read()
	*  8. demux <- extbus1
	*  9. pc -> intbus2
	* 10. ula <- intbus2
	* 11. ula.inc
	* 12. ula -> intbus2
	* 13. pc <- intbus2
	* 14. pc -> extbus1 
	* 15. memory.read() 
	* 16. memory.read()
	* 17. IR <- extbus1
	* 18. IR <- intbus2 
	* 19. ula <- intbus2 
	* 20. registers -> intbus2 
	* 21. ula <- intbus2
	* 22. ula.add  
	* 23. ula -> intbus2
	* 24. flags <- intbus2
	* 25. IR <- intbus2  
	* 26. pc -> extbus1 
	* 27. memory.read()
	* 28. memory.store() 
	* 29. IR -> extbus1
	* 30. memory.store()  
	* 31. pc -> intbus2
	* 32. ula <- intbus2
	* 33. ula.inc  
	* 34. ula -> intbus2        
	* 35. PC.internalStore()  
	*/  
    public void addRegMem() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        memory.read();
        IR.store();
        IR.internalRead();
        ula.internalStore(1);
        registersInternalRead();
        ula.internalStore(0);
        ula.add();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        IR.internalStore();
        PC.read();
        memory.read();
        memory.store();
        IR.read();
        memory.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }    
/*
	
	* ADD Imm Reg 
	*  1. pc -> intbus2         
	*  2. ula <- intbus2
	*  3. ula.inc	
	*  4. ula -> intbus2
	*  5. pc <- intbus2
	*  6. pc -> extbus1 
	*  7. memory.read() 
	*  8. IR <- extbus1
	*  9. pc -> intbus2  
	* 10. ula <- intbus2 
	* 11. ula.inc 
	* 12. ula -> intbus2  
	* 13. pc <- intbus2
	* 14. pc -> extbus1 
	* 15. memory.read() 
	* 16. demux <- extbus1
	* 17. registers -> intbus2
	* 18. ula <- intbus2 
	* 19. IR <- intbus2
	* 20. ula <- intbus2
	* 21. ula.add 
	* 22. ula -> intbus2
	* 23. flags <- intbus2
	* 24. registers -> intbus2
	* 25. pc -> intbus2 
	* 26. ula <- intbus2 
	* 27. ula.inc 
	* 28. ula -> intbus2 
	* 29. pc <- intbus2 
	* end
	*/
    public void addImmReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        IR.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        IR.internalRead();
        ula.internalStore(0);
        ula.add();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	*  SUB RegA RegB
	*  1. pc -> intbus2  
	*  2. ula <- intbus2
	*  3. ula.inc
	*  4. ula -> intbus2
	*  5. pc <- intbus2
	*  6. pc -> extbus1 
	*  7. memory.read()
	*  8. pc -> intbus2
	*  9. ula <- intbus2
	* 10. ula.inc 
	* 11. ula -> intbus2
	* 12. pc <- intbus2 
	* 13. demux <- extbus1
	* 14. registers -> intbus2 
	* 15. ula <- intbus2 
	* 16. pc -> extbus1
	* 17. memory.read()
	* 18. demux <- extbus1 
	* 19. registers -> intbus2 
	* 20. ula <- intbus2
	* 21. ula.add
	* 22. ula -> intbus2  
	* 23. flags <- intbus2
	* 24. registers -> intbus2
	* 25. pc -> intbus2
	* 26. ula <- intbus2 
	* 27. ula.inc     
	* 28. ula -> intbus2 
	* 29. pc <- intbus2 
	* end
	*/
    public void subRegARegB() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore(); 
    } 
	/*
	*  SUB Mem Reg 
	*  1. pc -> intbus2
	*  2. ula <- intbus2 
	*  3. ula.inc
	*  4. ula -> intbus2 
	*  5. pc <- intbus2 
	*  6. pc -> extbus1 
	*  7. memory.read() 
	*  8. memory.read() 
	*  9. IR <- extbus1 
	* 10. pc -> intbus2 
	* 11. ula <- intbus2
	* 12. ula.inc 
	* 13. ula -> intbus2
	* 14. pc <- intbus2 
	* 15. pc -> extbus1 
	* 16. memory.read() 
	* 17. demux <- extbus1
	* 18. registers -> intbus2
	* 19. ula <- intbus2 
	* 20. IR <- intbus2 
	* 21. ula <- intbus2
	* 22. ula.add   
	* 23. ula -> intbus2
	* 24. flags <- intbus2 
	* 25. registers -> intbus2 
	* 26. pc -> intbus2
	* 27. ula <- intbus2 
	* 28. ula.inc 
	* 29. ula -> intbus2  
	* 30. pc <- intbus2  
	* end
	*/
    public void subMemReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        memory.read();
        IR.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        IR.internalRead();
        ula.internalStore(0);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }  
/*
	
	*  SUB Reg Mem
	*  1. pc -> intbus2 
	*  2. ula <- intbus2
	*  3. ula.inc	
	*  4. ula -> intbus2  
	*  5. pc <- intbus2 
	*  6. pc -> extbus1     
	*  7. memory.read() 
	*  8. demux <- extbus1
	*  9. pc -> intbus2           
	* 10. ula <- intbus2          
	* 11. ula.inc                 
	* 12. ula -> intbus2          
	* 13. pc <- intbus2            
	* 14. pc -> extbus1           
	* 15. memory.read()             
	* 16. memory.read()          
	* 17. IR <- extbus1          
	* 18. IR <- intbus2          
	* 19. ula <- intbus2          
	* 20. registers -> intbus2     
	* 21. ula <- intbus2           
	* 22. ula.sub                  
	* 23. ula -> intbus2       
	* 24. flags <- intbus2          
	* 25. IR <- intbus2            
	* 26. pc -> extbus1          
	* 27. memory.read()            
	* 28. memory.store()           
	* 29. IR -> extbus1           
	* 30. memory.store()         
	* 31. pc -> intbus2          
	* 32. ula <- intbus2           
	* 33. ula.inc                  
	* 34. ula -> intbus2           
	* 35. PC.internalStore()        
	* end
	*/
    public void subRegMem() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        memory.read();
        IR.store();
        IR.internalRead();
        ula.internalStore(1);
        registersInternalRead();
        ula.internalStore(0);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        IR.internalStore();
        PC.read();
        memory.read();
        memory.store();
        IR.read();
        memory.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
	/*
	* SUB Imm Reg
	*  1. pc -> intbus2 
	*  2. ula <- intbus2 
	*  3. ula.inc	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2 
	*  6. pc -> extbus1  
	*  7. memory.read() 
	*  8. IR <- extbus1    
	*  9. pc -> intbus2          
	* 10. ula <- intbus2    
	* 11. ula.inc       
	* 12. ula -> intbus2   
	* 13. pc <- intbus2  ;
	* 14. pc -> extbus1    
	* 15. memory.read()      
	* 16. demux <- extbus1     
	* 17. registers -> intbus2   
	* 18. ula <- intbus2       
	* 19. IR <- intbus2        
	* 20. ula <- intbus2       
	* 21. ula.sub              
	* 22. ula -> intbus2          
	* 23. flags <- intbus2        
	* 24. registers -> intbus2     
	* 25. pc -> intbus2            
	* 26. ula <- intbus2           
	* 27. ula.inc                   
	* 28. ula -> intbus2            
	* 29. pc <- intbus2           
	* end
	*/ 
    public void subImmReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        IR.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        IR.internalRead();
        ula.internalStore(0);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
	/*
	*  JUMP
	*  1. pc -> intus2  
	*  2. ula <- intbus2 
	*  3. ula.incs
	*  4. ula -> intbus2 
	*  5. pc <- intbus2 
	*  6. pc -> extbus1    
	*  7. memory.read() 
	*  8. PC.store()  
	*  end  
	*/
    public void jmp() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        PC.store();
    }
	/*
	*  JZ 
	*  1. pc -> intus2 
	*  2. ula <- intbus2 
	*  3. ula.incs	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2 
	   ------------------
	*  6. pc -> extbus1  
	*  7. memory.read()
	*  8. CI: stn(1)         
	*  9. ula.inc                 
	* 10. ula -> intbus2  
	* 11. pc <- intbus2    
	* 12. pc -> extbus1       
	* 13. CI: stn(0) ← extbus1   
	* 14. extbus <- flags          
	* 15. statusMemory -> extbus1   
	* 16. pc <- extbus1            
	* end
	*/
    public void jz() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn1();
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn0();
        extbus1.put(Flags.getBit(0));
        statusMemory.read();
        PC.store();
    }
	/*
	*  JN 
	*  1. pc -> intus2 
	*  2. ula <- intbus2 
	*  3. ula.incs	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2
	*  6. pc -> extbus1   
	*  7. memory.read() 
	*  8. CI: stn(1) ← extbus1 
	*  9. ula.inc 
	* 10. ula -> intbus2 
	* 11. pc <- intbus2 
	* 12. pc -> extbus 
	* 13. CI: stn(0) ← extbus1  
	* 14. extbus <- flags
	* 15. statusMemory -> extbus   
	* 16. pc <- extbus             
	* end
	*/
    public void jn() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn1();
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn0();
        extbus1.put(Flags.getBit(1));
        statusMemory.read();
        PC.store();
    }
/*
	*  INC 
	*  1. pc -> intbus2   
	*  2. ula <- intbus2  
	*  3. ula.inc	
	*  4. ula -> intbus2  
	*  5. pc <- intbus2  
	*  6. pc -> extbus1   
	*  7. memory.read() 	
	*  8. demux <- extbus1 
	*  9. registers -> intbus2 
	* 10. ula <- intbus2   
	* 11. ula.inc    
	* 12. ula -> intbus2 
	* 13. flags <- intbus2      
	* 14. registers -> intbus2   
	* 15. pc -> intbus2     
	* 16. ula <- intbus2 
	* 17. ula.inc   
	* 18. ula -> intbus2   
	* 19. pc <- intbus2   
	* end
	*/
    public void inc() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	*  MOVE RegA RegB
	*  1. pc -> intbus2
	*  2. ula <- intbus2 
	*  3. ula.inc	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2 
	*  6. pc -> extbus1         
	*  7. memory.read() 		
	*  8. pc -> intbus2        
	*  9. ula <- intbus2        
	* 10. ula.inc                
	* 11. ula -> intbus2        
	* 12. pc <- intbus2           
	* 13. demux <- extbus1        
	* 14. registers -> intbus2 
	* 15. pc -> extbus1       
	* 16. memory.read()        
	* 17. demux <- extbus1    
	* 18. registers -> intbus2    
	* 19. pc -> intbus2        
	* 20. ula <- intbus2   
	* 21. ula.inc   
	* 22. ula -> intbus2  
	* 23. pc <- intbus2    
	* end
	*/
    public void moveRegARegB() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        demux.setValue(extbus1.get());
        registersInternalRead();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	*  MOVE Mem Reg
	*  1. pc -> intbus2 
	*  2. ula <- intbus2
	*  3. ula.inc
	*  4. ula -> intbus2
	*  5. pc <- intbus2
	*  6. pc -> extbus1    
	*  7. memory.read() 		
	*  8. memory.read() 	
	*  9. IR <- extbus1      
	* 10. ula.inc                
	* 11. ula -> intbus2         
	* 12. pc <- intbus2           
	* 13. IR <- intbus2             
	* 14. pc -> extbus1          
	* 15. memory.read()    
	* 16. demux <- extbus1  
	* 17. registers -> intbus2
	* 18. pc -> intbus2   
	* 19. ula <- intbus2  
	* 20. ula.inc     
	* 21. ula -> intbus2    
	* 22. pc <- intbus2      
	* end
	*/
    public void moveMemReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        memory.read();
        IR.store();
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        IR.internalRead();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }   
/*
	*   MOVE Reg Mem
	*  1. pc -> intbus2 
	*  2. ula <- intbus2  
	*  3. ula.inc	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2  
	*  6. pc -> extbus1    
	*  7. memory.read() 
	*  8. demux <- extbus1   
	*  9. pc -> intbus2  
	* 10. ula <- intbus2
	* 11. ula.inc          
	* 12. ula -> intbus2           
	* 13. pc <- intbus2           
	* 14. registers -> intbus2   
	* 15. IR <- intbus2         
	* 16. pc -> extbus1    
	* 17. memory.read() 	
	* 18. memory.store()  
	* 19. IR -> extbus1     
	* 20. memory.store()      
	* 21. pc -> intbus2  
	* 22. ula <- intbus2     
	* 23. ula.inc     
	* 24. ula -> intbus2 
	* 25. pc <- intbus2    
	* end
	*/
    public void moveRegMem() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        registersInternalRead();
        IR.internalStore();
        PC.read();
        memory.read();
        memory.store();
        IR.read();
        memory.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
/*
	*  MOVE Imm Reg --
	*  1. pc -> intbus2  
	*  2. ula <- intbus2
	*  3. ula.inc	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2  
	*  6. pc -> extbus1   
	*  7. memory.read() 	
	*  8. IR <- extbus1
	*  9. pc -> intbus2     
	* 10. ula <- intbus2   
	* 11. ula.inc    
	* 12. ula -> intbus2      
	* 13. pc <- intbus2   
	* 14. pc -> extbus1   
	* 15. memory.read() 	
	* 16. demux <- extbus1         
	* 17. IR <- intbus2          
	* 18. registers -> intbus2    
	* 19. pc -> intbus2            
	* 20. ula <- intbus2          
	* 21. ula.inc               
	* 22. ula -> intbus2          
	* 23. pc <- intbus2             
	* end
	*/

    public void moveImmReg() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        IR.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        IR.internalRead();
        registersInternalStore();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
	/*
	*  JEQ
	*  1. pc -> intbus2  
	*  2. ula <- intbus2 
	*  3. ula.inc		
	*  4. ula -> intbus2  
	*  5. pc <- intbus2  
	*  6. pc -> extbus1      
	*  7. memory.read() 		  
	*  8. demux <- extbus1       
	*  9. registers -> intbus2   
	* 10. ula <- intbus2    
	* 11. ula.inc     
	* 12. ula -> intbus2     
	* 13. pc <- intbus2       
	* 14. pc -> extbus1   
	* 15. memory.read()     
	* 16. demux <- extbus1  
	* 17. registers -> intbus2   
	* 18. ula <- intbus2    
	* 19. ula.sub              
	* 20. ula -> intbus2           
	* 21. flags <- intbus2        
	* 22. pc -> intbus2        
	* 23. ula <- intbus2        
	* 24. ula.inc                   
	* 25. ula -> intbus2       
	* 26. pc <- intbus2       
	* 27. pc -> extbus1     
	* 28. memory.read()   
	* 29. CI: stn(1) ← extbus1
	* 30. ula.inc      
	* 31. ula -> intbus2       
	* 32. pc <- intbus2      
	* 33. pc -> extbus1            
	* 34. CI: stn(0) ← extbus1    
	* 35. extbus1 <- flags          
	* 36. statusMemory -> extbus1  
	* 37. pc <- extbus1           
    * end
	*/

    public void jeq() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read(); 
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn1();
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn0();
        extbus1.put(Flags.getBit(0));
        statusMemory.read();
        PC.store();
    }

/*
	*  JNEQ 
	*  1. pc -> intbus2  
	*  2. ula <- intbus2 
	*  3. ula.inc   
	*  4. ula -> intbus2  
	*  5. pc <- intbus2   
	*  6. pc -> extbus1    
	*  7. memory.read() 	
	*  8. demux <- extbus1     
	*  9. registers -> intbus2
	* 10. ula <- intbus2          
	* 11. PC -> intbus2       
	* 12. ula <- intbus2      
	* 13. ula.inc             
	* 14. ula -> intbus2         
	* 15. pc <- intbus2        
	* 16. pc -> extbus1    
	* 17. memory.read()            
	* 18. demux <- extbus1        
	* 19. registers -> intbus2   
	* 20. ula <- intbus2      
	* 21. ula.sub       
	* 22. ula -> intbus2    
	* 23. flags <- intbus2      
	* 24. pc -> intbus2        
	* 25. ula <- intbus2    
	* 26. ula.inc      
	* 27. ula -> intbus2      
	* 28. pc <- intbus2         
	* 29. pc -> extbus1        
	* 30. memory.read()        
	* 31. CI: stn(0) ← extbus1     
	* 32. PC -> intbus2           
	* 33. ula <- intbus2           
	* 34. ula.inc                 
	* 35. ula -> intbus2         
	* 36. pc <- intbus2          
	* 37. pc -> extbus1         
	* 38. CI: stn(1) ← extbus1    
	* 39. extbus1 <- flags     
	* 40. statusMemory -> extbus1   
	* 41. pc <- extbus1     
	* end
	*/
    public void jneq() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn0();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn1();
        extbus1.put(Flags.getBit(0));
        statusMemory.read();
        PC.store();
    }
	/*
	* JGT
	*  1. pc -> intbus2 
	*  2. ula <- intbus2
	*  3. ula.inc
	*  4. ula -> intbus2 
	*  5. pc <- intbus2
	*  6. pc -> extbus1       
	*  7. memory.read() 
	*  8. demux <- extbus1     
	*  9. registers -> intbus2 
	* 10. ula <- intbus2 
	* 11. PC -> intbus2     
	* 12. ula <- intbus2      
	* 13. ula.inc   
	* 14. ula -> intbus2   
	* 15. pc <- intbus2     
	* 16. pc -> extbus1
	* 17. memory.read()   
	* 18. demux <- extbus1         
	* 19. registers -> intbus2    
	* 20. ula <- intbus2     
	* 21. ula.sub            
	* 22. ula -> intbus2        
	* 23. flags <- intbus2       
	* 24. pc -> intbus2        
	* 25. ula <- intbus2   
	* 26. ula.inc      
	* 27. ula -> intbus2           
	* 28. pc <- intbus2         
	* 29. pc -> extbus1         
	* 30. memory.read()          
	* 31. CI: stn(0) ← extbus1      
	* 32. PC -> intbus2         
	* 33. ula <- intbus2         
	* 34. ula.inc            
	* 35. ula -> intbus2      
	* 36. pc <- intbus2      
	* 37. pc -> extbus1       
	* 38. CI: stn(1) ← extbus1   
	* 39. extbus1 <- flags      
	* 40. IR <- extbus1     
	* 41. IR -> intbus2            
	* 42. ula <- intbus2         
	* 43. extbus1 <- flags          
	* 44. IR <- extbus1           
	* 45. IR -> intbus2          
	* 46. ula <- intbus2           
	* 47. ula.add                
	* 48. ula -> intbus2         
	* 49. IR <- intbus2           
	* 50. IR -> extbus1        
	* 51. statusMemory -> extbus1  
	* 52. pc <- extbus1 
	* end 
	*/
    public void jgt() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn0();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn1();
        extbus1.put(Flags.getBit(1));
        IR.store();
        IR.internalRead();
        ula.internalStore(1);
        extbus1.put(Flags.getBit(0));
        IR.store();
        IR.internalRead();
        ula.internalStore(0);
        ula.add();
        ula.internalRead(1);
        IR.internalStore();
        IR.read();
        statusMemory.read();
        PC.store();
    }
	/*
	*  JLW
	*  1. pc -> intbus2 
	*  2. ula <- intbus2
	*  3. ula.inc 	
	*  4. ula -> intbus2 
	*  5. pc <- intbus2  
	*  6. pc -> extbus1    
	*  7. memory.read() 	
	*  8. demux <- extbus1    
	*  9. registers -> intbus2  
	* 10. ula <- intbus2       
	* 11. PC -> intbus2    
	* 12. ula <- intbus2            
	* 13. ula.inc             
	* 14. ula -> intbus2     
	* 15. pc <- intbus2       
	* 16. pc -> extbus1     
	* 17. memory.read()          
	* 18. demux <- extbus1    
	* 19. registers -> intbus2     
	* 20. ula <- intbus2        
	* 21. ula.sub            
	* 22. ula -> intbus2        
	* 23. flags <- intbus2      
	* 24. pc -> intbus2       
	* 25. ula <- intbus2            
	* 26. ula.inc              
	* 27. ula -> intbus2       
	* 28. pc <- intbus2           
	* 29. pc -> extbus1        
	* 30. memory.read()        
	* 31. CI: stn(0) ← extbus1   
	* 32. PC -> intbus2            
	* 33. ula <- intbus2         
	* 34. ula.inc                
	* 35. ula -> intbus2         
	* 36. pc <- intbus2           
	* 37. pc -> extbus1            
	* 38. CI: stn(1) ← extbus1     
	* 39. extbus1 <- flags         
	* 40. statusMemory -> extbus1   
	* 41. pc <- extbus1         
	* end 
	*/
    public void jlw() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(0);
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        demux.setValue(extbus1.get());
        registersInternalRead();
        ula.internalStore(1);
        ula.sub();
        ula.internalRead(1);
        setStatusFlags(intbus2.get());
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        statusMemory.storeIn1();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        statusMemory.storeIn0();
        extbus1.put(Flags.getBit(1));
        statusMemory.read();
        PC.store();
    }
	/*
	*  CALL 
	*  1. PC -> intbus2 
	*  2. ula <- intbus2 
	*  3. ula.inc    
	*  4. ula -> intbus2 
	*  5. PC <- intbus2  
	*  6. PC -> extbus1       
	*  7. memory.read() 	
	*  8. PC <- extbus1        
	*  9. ula.inc              
	* 10. ula -> intbus2     
	* 11. IR <- intbus2          
	* 12. StkTOP -> extbus1    
	* 13. memory.store()      
	* 14. IR -> extbus1      
	* 15. memory.store()         
	* 16. StkTOP -> intbus2    
	* 17. IR <- extbus1  
	* 18. IR -> intbus2   
	* 19. ula <- intbus2       
	* 20. ula.inc  
	* 21. ula <- intbus2  
	* 22. ula.sub        
	* 23. ula.add             
	* 24. ula -> intbus2           
	* 25. IR <- intbus2            
	* 26. IR -> extbus1         
	* stkTOP <- extbus1         
	* end 
	*/
    public void call() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        PC.store();
        ula.inc();
        ula.internalRead(1);
        IR.internalStore();
        StkTOP.read();
        memory.store();
        IR.read();
        memory.store();
        StkTOP.read(); 
        IR.store();
        IR.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalStore(0);
        ula.sub();
        ula.add();
        ula.internalRead(1);
        IR.internalStore();
        IR.read();
        StkTOP.store();
    }
/*
	*  RET 
	*  1. stkTOP -> extbus1    
	*  2. IR <- extbus1      
	*  3. IR -> intbus2      
	*  4. ula <- intbus2      
	*  5. ula.inc       
	*  6. ula -> intbus2        
	*  7. IR <- intbus2        
	*  8. IR -> extbus1         
	*  9. stkTOP <- extbus1   
	* 10. memory.read()        
	* 11. pc <- extbus1         
	* end 
	*/
    public void ret() {
        StkTOP.read();
        IR.store();
        IR.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        IR.internalStore();
        IR.read();
        StkTOP.store();
        memory.read();
        PC.store();
    }
/*
	*  S-Stack
	*  1. PC -> intbus2  
	*  2. ula <- intbus2 
	*  3. ula.inc  
	*  4. ula -> intbus2
	*  5. PC <- intbus2  
	*  6. PC -> extbus1     
	*  7. memory.read() 		
	*  8. StkBOT <- extbus1     
	*  9. StkTOP <- extbus1    
	* 10. PC -> intbus2            
	* 11. ula <- intbus2      
	* 12. ula.inc                 
	* 13. ula -> intbus2           
	* 14. PC <- intbus2           
	* end
	*/
    public void startStk() {
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
        PC.read();
        memory.read();
        StkBOT.store();
        StkTOP.store();
        PC.internalRead();
        ula.internalStore(1);
        ula.inc();
        ula.internalRead(1);
        PC.internalStore();
    }
    public ArrayList<Register> getRegistersList() {
        return registersList;
    }

    /**
     * This method performs an (external) read from a register into the register
     * list. The register id must be in the demux bus
     */
    private void registersRead() {
        registersList.get(demux.getValue()).read();
    }

    /**
     * This method performs an (internal) read from a register into the register
     * list. The register id must be in the demux bus
     */
    private void registersInternalRead() {
        registersList.get(demux.getValue()).internalRead();;
    }

    /**
     * This method performs an (external) store toa register into the register
     * list. The register id must be in the demux bus
     */
    private void registersStore() {
        registersList.get(demux.getValue()).store();
    }

    /**
     * This method performs an (internal) store toa register into the register
     * list. The register id must be in the demux bus
     */
    private void registersInternalStore() {
        registersList.get(demux.getValue()).internalStore();;
    }

    /**
     * This method reads an entire file in machine code and stores it into the
     * memory 
     * @param filename
     * @throws IOException
     */
    public void readExec(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename + ".dxf"));
        String linha;
        int i = 0;
        while ((linha = br.readLine()) != null) {
            extbus1.put(i);
            memory.store();
            extbus1.put(Integer.parseInt(linha));
            memory.store();
            i++;
        }
        br.close();
    }

    /**
     * This method executes a program that is stored in the memory
     */
    public void controlUnitEexec() {
        halt = false;
        while (!halt) {
            fetch();
            decodeExecute();
        }

    }

    /**
     * This method implements The decode proccess, that is to find the correct
     * operation do be executed according the command. And the execute proccess,
     * that is the execution itself of the command
     */
    private void decodeExecute() {
        IR.internalRead(); //the instruction is in the internalbus2
        int command = intbus2.get();
        simulationDecodeExecuteBefore(command);
        switch (command) {
            case 0:
                addMemReg();
                break;
            case 1:
                addRegMem();
                break;
            case 2:
                addRegARegB();
                break;
            case 3:
                addImmReg();
                break;
            case 4:
                subRegARegB();
                break;
            case 5:
                subMemReg();
                break;
            case 6:
                subRegMem();
                break;
            case 7:
                subImmReg();
                break;
            case 8:
                jmp();
                break;
            case 9:
                jz();
                break;
            case 10:
                jn();
                break;
            case 11:
                inc();
                break;
            case 12:
                moveMemReg();
                break;
            case 13:
                moveRegMem();
                break;
            case 14:
                moveRegARegB();
                break;
            case 15:
                moveImmReg();
                break;
            case 16:
                jeq();
                break;
            case 17:
                jneq();
                break;
            case 18:
                jgt();
                break;
            case 19:
                jlw();
                break;
            case 20:
                call();
                break;
            case 21:
                ret();
                break;
            case 22:
                startStk();
                break;
            default:
                halt = true;
                break;
        }
        if (simulation) {
            simulationDecodeExecuteAfter();
        }
    }

    /**
     * This method is used to show the components status in simulation
     * conditions NOT TESTED
     *
     * @param command
     */
    private void simulationDecodeExecuteBefore(int command) {
        System.out.println("----------BEFORE Decode and Execute phases--------------");
        String instruction;
        int param1 = 0, param2 = 0, param3 = 0;

        for (Register reg : registersList) {
            System.out.println(reg.getRegisterName() + ": " + reg.getData());
        }
        
        if (command != -1) {
            instruction = commandsList.get(command);
        } else {
            instruction = "END";
        }
        
        // Determinando número de operandos (1-3)
        int operandCount = hasOperands(instruction);

        int currentPC = PC.getData();
        int totalMemory = memory.getDataList().length;

        switch (operandCount) {
            case 1:
                if (currentPC + 1 < totalMemory) {
                    param1 = memory.getDataList()[currentPC + 1];
                    System.out.println("Instrução: " + instruction + " " + param1);
                } else {
                    System.out.println("Instrução: " + instruction + " (operando inválido)");
                }
                break;
                
            case 2:
                if (currentPC + 2 < totalMemory) {
                    param1 = memory.getDataList()[currentPC + 1];
                    param2 = memory.getDataList()[currentPC + 2];
                    System.out.println("Instrução: " + instruction + " " + param1 + " " + param2);
                } else {
                    System.out.println("Instrução: " + instruction + " (operandos inválidos)");
                }
                break;
                
            case 3:
                if (currentPC + 3 < totalMemory) {
                    param1 = memory.getDataList()[currentPC + 1];
                    param2 = memory.getDataList()[currentPC + 2];
                    param3 = memory.getDataList()[currentPC + 3];
                    System.out.println("Instrução: " + instruction + " " + param1 + " " + param2 + " " + param3);
                } else {
                    System.out.println("Instrução: " + instruction + " (operandos inválidos)");
                }
                break;
                
            default:
                System.out.println("Instrução: " + instruction);
                if ("read".equals(instruction) && param1 >= 0 && param1 < totalMemory) {
                    System.out.println("memoria[" + param1 + "] = " + memory.getDataList()[param1]);
                }
                break;
        }
    }
    /**
     * This method is used to show the components status in simulation
     * conditions NOT TESTED
     */
    private void simulationDecodeExecuteAfter() {
        String instruction;
        System.out.println("-----------AFTER Decode and Execute phases--------------");
        System.out.println("Internal Bus 1: " + intbus1.get());
        System.out.println("Internal Bus 2: " + intbus2.get());
        System.out.println("External Bus 1: " + extbus1.get());
        for (Register r : registersList) {
            System.out.println(r.getRegisterName() + ": " + r.getData());
        }

        // Verificação do valor base da pilha
        int baseStackValue = memory.getDataList()[1];
        if (baseStackValue >= memorySize) {
            baseStackValue = memorySize - 1;
        }
        
        // Exibição do conteúdo da memória se a chave estiver ativa
        if (MemoryKey) {
            int memoryIndex = memorySize - 1;
            while (memoryIndex > 0) {
                System.out.println("Posição de Memória [" + memoryIndex + "]: " + memory.getDataList()[memoryIndex]);
                memoryIndex--;
            }
        }
        
        Scanner entrada = new Scanner(System.in);
        System.out.println("Press <Enter>");
        String mensagem = entrada.nextLine();
    }
    /**
     * This method uses PC to find, in the memory, the command code that must be
     * executed. This command must be stored in IR NOT TESTED!
     */
    private void fetch() {
        PC.read();
        memory.read();
        IR.store();
        simulationFetch();
    }

    /**
     * This method is used to show the components status in simulation
     * conditions NOT TESTED!!!!!!!!!
     */
    private void simulationFetch() {
        if (simulation) {
            System.out.println("-------Fetch Phase------");
            System.out.println("PC: " + PC.getData());
            System.out.println("IR: " + IR.getData());
        }
    }

    /**
     * This method is used to show in a correct way the operands (if there is
     * any) of instruction, when in simulation mode NOT TESTED!!!!!
     *
     * @param instruction
     * @return
     */
    private int hasOperands(String instruction) {
        if ("ret".equals(instruction)) {
            return 0;
        } else if ("jlw".equals(instruction) || "jgt".equals(instruction) || "jneq".equals(instruction)|| "jeq".equals(instruction)) {
            return 3;
        } else if ("jn".equals(instruction)|| "jz".equals(instruction) || "jmp".equals(instruction) || "inc".equals(instruction)|| "call".equals(instruction) || "startStk".equals(instruction)) {
            return 1;
        } else {
            return 2;
        }
    }

    
    public int getMemorySize() {
        return memorySize;
    }
    public static void main(String[] args) throws IOException {
        // Validação simples de argumentos
        if (args.length != 1) {
            System.out.println("Voce precisa colocar argumentos. Uso correto: <nome_do_programa_sem_extensão>");
            return;
        }
        String programName = args[0];
        File input = new File(programName + ".dxf");

        // Verificação de existência do arquivo
        if (!input.exists()) {
            System.out.println("Arquivo não encontrado: " + programName + ".dxf");
            return;
        }
        Architecture arch = new Architecture(true, false);
        arch.readExec(programName);
        arch.controlUnitEexec();
    }

}
