function maxPressure(desiredSpeed, index)
    fid = fopen(sprintf("./output/desiredSpeeds/ovito_file_DS=%.1f_%d.txt", desiredSpeed, index));
    fmax = 0.0;
    # Read file
    while (!feof(fid))
        N = fgetl(fid)
        # Parse current N
        if(N == -1)
            break
        endif
        N = str2num(N);
        # Parse current t
        str2num(fgetl(fid));
        imax = max(dlmread(fid, ' ', sprintf("G1..G%d", N)));
        if(imax > fmax)
            fmax = imax;
        endif
    endwhile
    fclose(fid);
    disp(fmax);
end